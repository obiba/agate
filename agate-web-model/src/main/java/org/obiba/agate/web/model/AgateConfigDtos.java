package org.obiba.agate.web.model;

import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.AgateConfig;
import org.springframework.stereotype.Component;

@Component
class AgateConfigDtos {

  @NotNull
  Agate.AgateConfigDto asDto(@NotNull AgateConfig config) {
    Agate.AgateConfigDto.Builder builder = Agate.AgateConfigDto.newBuilder() //
        .setName(config.getName());
    if(config.hasDomain()) builder.setDomain(config.getDomain());
    if(config.hasPublicUrl()) builder.setPublicUrl(config.getPublicUrl());
    return builder.build();
  }

  @NotNull
  AgateConfig fromDto(@NotNull Agate.AgateConfigDtoOrBuilder dto) {
    AgateConfig config = new AgateConfig();
    config.setName(dto.getName());
    if(dto.hasDomain()) config.setDomain(dto.getDomain());
    if(dto.hasPublicUrl()) config.setPublicUrl(dto.getPublicUrl());
    return config;
  }

}
