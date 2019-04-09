package org.obiba.agate.web.model;

import com.google.common.base.Strings;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.RealmConfig;
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
      .setForSignup(config.isForSignup());

    if(!Strings.isNullOrEmpty(config.getTitle())) builder.setTitle(config.getTitle());
    if(!Strings.isNullOrEmpty(config.getDescription())) builder.setDescription(config.getDescription());
    if(config.hasGroups()) {
      builder.addAllGroups(config.getGroups());
    }

    return builder.build();
  }

  @NotNull
  public RealmConfig fromDto(RealmConfigDto dto) {
    RealmConfig config = new RealmConfig();
    config.setId(dto.getId());
    config.setName(dto.getName());
    config.setRealm(AgateRealm.valueOf(dto.getName()));
    config.setDefaultRealm(dto.getDefaultRealm());
    config.setForSignup(dto.getForSignup());
    config.setGroups(dto.getGroupsList());
    if (dto.hasTitle()) config.setTitle(dto.getTitle());
    if (dto.hasDescription()) config.setTitle(dto.getDescription());

    return config;
  }

}
