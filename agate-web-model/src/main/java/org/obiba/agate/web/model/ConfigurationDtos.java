package org.obiba.agate.web.model;

import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.Configuration;
import org.springframework.stereotype.Component;

@Component
class ConfigurationDtos {

  @NotNull
  Agate.ConfigurationDto asDto(@NotNull Configuration configuration) {
    Agate.ConfigurationDto.Builder builder = Agate.ConfigurationDto.newBuilder() //
        .setName(configuration.getName()) //
        .setShortTimeout(configuration.getShortTimeout()) //
        .setLongTimeout(configuration.getLongTimeout());
    if(configuration.hasDomain()) builder.setDomain(configuration.getDomain());

    return builder.build();
  }

  @NotNull
  Configuration fromDto(@NotNull Agate.ConfigurationDtoOrBuilder dto) {
    Configuration configuration = new Configuration();
    configuration.setName(dto.getName());
    if(dto.hasDomain()) configuration.setDomain(dto.getDomain());
    configuration.setShortTimeout(dto.getShortTimeout());
    configuration.setLongTimeout(dto.getLongTimeout());
    return configuration;
  }

}
