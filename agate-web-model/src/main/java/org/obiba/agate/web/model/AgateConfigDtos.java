package org.obiba.agate.web.model;

import java.util.Locale;

import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.AgateConfig;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
class AgateConfigDtos {

  @NotNull
  Agate.AgateConfigDto asDto(@NotNull AgateConfig config) {
    Agate.AgateConfigDto.Builder builder = Agate.AgateConfigDto.newBuilder() //
        .setName(config.getName()) //
        .setDefaultCharSet(config.getDefaultCharacterSet());
    config.getLocales().forEach(locale -> builder.addLanguages(locale.getLanguage()));
    if(!Strings.isNullOrEmpty(config.getPublicUrl())) {
      builder.setPublicUrl(config.getPublicUrl());
    }
    return builder.build();
  }

  @NotNull
  AgateConfig fromDto(@NotNull Agate.AgateConfigDtoOrBuilder dto) {
    AgateConfig config = new AgateConfig();
    config.setName(dto.getName());
    config.setDefaultCharacterSet(dto.getDefaultCharSet());
    if(dto.hasPublicUrl()) config.setPublicUrl(dto.getPublicUrl());
    dto.getLanguagesList().forEach(lang -> config.getLocales().add(new Locale(lang)));
    return config;
  }

}
