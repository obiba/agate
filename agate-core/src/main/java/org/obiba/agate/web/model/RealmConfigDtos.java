package org.obiba.agate.web.model;

import com.google.common.base.Strings;
import org.json.JSONException;
import org.obiba.agate.domain.*;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.web.model.Agate.RealmConfigDto;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

@Component
public class RealmConfigDtos {

  private final ConfigurationService configurationService;

  @Inject
  public RealmConfigDtos(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }

  @NotNull
  RealmConfigDto asDto(RealmConfig config) {
    RealmConfigDto.Builder builder = RealmConfigDto.newBuilder()
      .setId(config.getId())
      .setName(config.getName())
      .setType(config.getType().getName())
      .setDefaultRealm(config.isDefaultRealm())
      .setForSignup(config.isForSignup())
      .setStatus(Agate.RealmStatus.valueOf(config.getStatus().toString()))
      .addAllGroups(config.getGroups())
      .setContent(ensureSecuredContent(configurationService.decrypt(config.getContent()), config.getType()));

    if(!Strings.isNullOrEmpty(config.getTitle())) builder.setTitle(config.getTitle());
    if(!Strings.isNullOrEmpty(config.getDescription())) builder.setDescription(config.getDescription());

    return builder.build();
  }

  @NotNull
  RealmConfig fromDto(RealmConfigDto dto) {
    RealmConfig.Builder builder = RealmConfig.newBuilder();
    if (dto.hasId()) builder.id(dto.getId());
    builder.name(dto.getName());
    builder.type(AgateRealm.fromString(dto.getType()));
    builder.defaultRealm(dto.getDefaultRealm());
    builder.forSignup(dto.getForSignup());
    builder.groups(dto.getGroupsList());

    if (dto.hasContent() && !Strings.isNullOrEmpty(dto.getContent())) {
      builder.content(configurationService.encrypt(dto.getContent()));
    }

    if (dto.hasTitle()) builder.title(dto.getTitle());
    if (dto.hasDescription()) builder.description(dto.getDescription());
    if (dto.hasStatus()) builder.status(RealmStatus.valueOf(dto.getStatus().name()));

    return builder.build();
  }

  @NotNull
  Agate.RealmConfigSummaryDto asSummaryDto(RealmConfig config) {
    Agate.RealmConfigSummaryDto.Builder builder = Agate.RealmConfigSummaryDto.newBuilder()
      .setId(config.getId())
      .setName(config.getName())
      .setType(config.getType().getName())
      .setStatus(Agate.RealmStatus.valueOf(config.getStatus().toString()));

    if(!Strings.isNullOrEmpty(config.getTitle())) builder.setTitle(config.getTitle());
    if(!Strings.isNullOrEmpty(config.getDescription())) builder.setDescription(config.getDescription());
    return builder.build();
  }

  private String ensureSecuredContent(String content, AgateRealm agateRealm) {
    if (!Strings.isNullOrEmpty(content)) {
      try {
        switch (agateRealm) {
          case AGATE_LDAP_REALM:
            return LdapRealmConfig.newBuilder(content).build().getAsSecuredJSONObject().toString();
          case AGATE_JDBC_REALM:
            return JdbcRealmConfig.newBuilder(content).build().getAsSecuredJSONObject().toString();
        }
      } catch (JSONException e) {
        throw new IllegalStateException("Realm config content is not a valid JSON.");
      }
    }

    return content;
  }
}
