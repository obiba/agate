package org.obiba.agate.web.model;

import com.google.common.base.Strings;
import jakarta.annotation.Nonnull;
import org.json.JSONException;
import org.obiba.agate.domain.*;
import org.obiba.agate.repository.UserRepository;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.support.UserInfoFieldsComparator;
import org.obiba.agate.web.model.Agate.RealmConfigDto;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RealmConfigDtos {

  private final LocalizedStringDtos localizedStringDtos;
  private final ConfigurationService configurationService;
  private final UserRepository userRepository;

  @Inject
  public RealmConfigDtos(LocalizedStringDtos localizedStringDtos, ConfigurationService configurationService, UserRepository userRepository) {
    this.localizedStringDtos = localizedStringDtos;
    this.configurationService = configurationService;
    this.userRepository = userRepository;
  }

  @Nonnull
  RealmConfigDto.Builder asDtoBuilder(RealmConfig config) {
    RealmConfigDto.Builder builder = RealmConfigDto.newBuilder()
      .setId(config.getId())
      .setName(config.getName())
      .setType(config.getType().getName())
      .setForSignup(config.isForSignup())
      .setStatus(Agate.RealmStatus.valueOf(config.getStatus().toString()))
      .addAllGroups(config.getGroups())
      .setContent(ensureSecuredContent(configurationService.decrypt(config.getContent()), config.getType()))
      .setUserCount(userRepository.countByRealm(config.getName()));

    if (config.getTitle() != null) builder.addAllTitle(localizedStringDtos.asDto(config.getTitle()));
    if (config.getDescription() != null) builder.addAllDescription(localizedStringDtos.asDto(config.getDescription()));
    if (config.hasPublicUrl()) builder.setPublicUrl(config.getPublicUrl());
    if (config.hasDomain()) builder.setDomain(config.getDomain());

    Optional.ofNullable(config.getUserInfoMapping()).ifPresent(map -> {
      List<RealmConfigDto.UserInfoMappingDto> userInfoMappings = map.entrySet()
        .stream()
        .sorted(UserInfoFieldsComparator::compare)
        .map(entry -> RealmConfigDto.UserInfoMappingDto
          .newBuilder()
          .setKey(entry.getKey())
          .setValue(entry.getValue()).build()
        )
        .collect(Collectors.toList());

      builder.addAllUserInfoMappings(userInfoMappings);
    });

    return builder;
  }

  @Nonnull
  RealmConfig fromDto(RealmConfigDto dto) {
    RealmConfig.Builder builder = RealmConfig.newBuilder();
    if (dto.hasId()) builder.id(dto.getId());
    builder.name(dto.getName());
    builder.type(AgateRealm.fromString(dto.getType()));
    builder.forSignup(dto.getForSignup());
    builder.groups(dto.getGroupsList());

    if (dto.hasPublicUrl()) {
      builder.publicUrl(dto.getPublicUrl());
    }

    if (dto.hasDomain()) {
      builder.domain(dto.getDomain());
    }

    if (dto.hasContent() && !Strings.isNullOrEmpty(dto.getContent())) {
      builder.content(configurationService.encrypt(dto.getContent()));
    }

    if (dto.getTitleCount() > 0) builder.title(localizedStringDtos.fromDto(dto.getTitleList()));
    if (dto.getDescriptionCount() > 0) builder.description(localizedStringDtos.fromDto(dto.getDescriptionList()));
    if (dto.hasStatus()) builder.status(RealmStatus.valueOf(dto.getStatus().name()));
    if (dto.getUserInfoMappingsCount() > 0) {
      builder.mapping(
        dto.getUserInfoMappingsList()
          .stream()
          .collect(Collectors.toMap(
            RealmConfigDto.UserInfoMappingDto::getKey,
            RealmConfigDto.UserInfoMappingDto::getValue)
          )
      );
    }

    return builder.build();
  }

  @Nonnull
  Agate.RealmConfigSummaryDto asSummaryDto(RealmConfig config) {
    Agate.RealmConfigSummaryDto.Builder builder = Agate.RealmConfigSummaryDto.newBuilder()
      .setId(config.getId())
      .setName(config.getName())
      .setType(config.getType().getName())
      .setStatus(Agate.RealmStatus.valueOf(config.getStatus().toString()))
      .setUserCount(userRepository.countByRealm(config.getName()));

    if (config.getTitle() != null) builder.addAllTitle(localizedStringDtos.asDto(config.getTitle()));
    if (config.getDescription() != null) builder.addAllDescription(localizedStringDtos.asDto(config.getDescription()));

    return builder.build();
  }

  private String ensureSecuredContent(String content, AgateRealm agateRealm) {
    if (!Strings.isNullOrEmpty(content)) {
      try {
        switch (agateRealm) {
          case AGATE_LDAP_REALM:
            return LdapRealmConfig.newBuilder(content).build().getAsSecuredJSONObject().toString();
          case AGATE_AD_REALM:
            return ActiveDirectoryRealmConfig.newBuilder(content).build().getAsSecuredJSONObject().toString();
          case AGATE_JDBC_REALM:
            return JdbcRealmConfig.newBuilder(content).build().getAsSecuredJSONObject().toString();
          case AGATE_OIDC_REALM:
            return OidcRealmConfig.newBuilder(content).build().getAsSecuredJSONObject().toString();
        }
      } catch (JSONException e) {
        throw new IllegalStateException("Realm config content is not a valid JSON.");
      }
    }

    return content;
  }
}
