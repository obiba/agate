package org.obiba.agate.web.model;

import com.google.common.base.Strings;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.domain.RealmStatus;
import org.obiba.agate.repository.GroupRepository;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.agate.web.model.Agate.RealmConfigDto;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

@Component
public class RealmConfigDtos {

  private final RealmConfigService configService;

  private final GroupRepository groupRepository;

  @Inject
  public RealmConfigDtos(RealmConfigService configService, GroupRepository groupRepository) {
    this.configService = configService;
    this.groupRepository = groupRepository;
  }

  @NotNull
  public RealmConfigDto asDto(RealmConfig config) {
    RealmConfigDto.Builder builder = RealmConfigDto.newBuilder()
      .setId(config.getId())
      .setName(config.getName())
      .setRealm(config.getRealm().getName())
      .setDefaultRealm(config.isDefaultRealm())
      .setForSignup(config.isForSignup())
      .setStatus(Agate.RealmStatus.valueOf(config.getStatus().toString()));

    if(!Strings.isNullOrEmpty(config.getTitle())) builder.setTitle(config.getTitle());
    if(!Strings.isNullOrEmpty(config.getDescription())) builder.setDescription(config.getDescription());
    if(config.hasGroups()) {
      builder.addAllGroups(config.getGroups());
    }

    return builder.build();
  }

  @NotNull
  public RealmConfig fromDto(RealmConfigDto dto) {
    RealmConfig.Builder builder = RealmConfig.newBuilder();
    if (dto.hasId()) builder.id(dto.getId());
    builder.name(dto.getName());
    builder.realm(AgateRealm.fromString(dto.getRealm()));
    builder.defaultRealm(dto.getDefaultRealm());
    builder.forSignup(dto.getForSignup());
    builder.setGroups(dto.getGroupsList());
    if (dto.hasTitle()) builder.title(dto.getTitle());
    if (dto.hasDescription()) builder.description(dto.getDescription());
    if (dto.hasStatus()) builder.status(RealmStatus.valueOf(dto.getStatus().name()));

    return builder.build();
  }

  @NotNull
  public Agate.RealmConfigSummaryDto asSummaryDto(RealmConfig config) {
    Agate.RealmConfigSummaryDto.Builder builder = Agate.RealmConfigSummaryDto.newBuilder()
      .setId(config.getId())
      .setName(config.getName());

    if(!Strings.isNullOrEmpty(config.getTitle())) builder.setTitle(config.getTitle());
    if(!Strings.isNullOrEmpty(config.getDescription())) builder.setDescription(config.getDescription());
    return builder.build();
  }
}
