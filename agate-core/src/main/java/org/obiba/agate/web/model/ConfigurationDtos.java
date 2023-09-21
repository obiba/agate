/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.model;

import jakarta.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.LocaleUtils;
import org.obiba.agate.domain.AttributeConfiguration;
import org.obiba.agate.domain.Configuration;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
class ConfigurationDtos {


  @Inject
  private LocalizedStringDtos localizedStringDtos;

  @NotNull
  Agate.ConfigurationDto asDto(@NotNull Configuration configuration) {
    Agate.ConfigurationDto.Builder builder = Agate.ConfigurationDto.newBuilder()
      .setName(configuration.getName())
      .setShortTimeout(configuration.getShortTimeout())
      .setLongTimeout(configuration.getLongTimeout())
      .setInactiveTimeout(configuration.getInactiveTimeout())
      .setJoinPageEnabled(configuration.isJoinPageEnabled())
      .setJoinWithUsername(configuration.isJoinWithUsername());

    configuration.getLocales().forEach(locale -> builder.addLanguages(locale.getLanguage()));

    if(configuration.hasDomain()) builder.setDomain(configuration.getDomain());
    if(configuration.hasPublicUrl()) builder.setPublicUrl(configuration.getPublicUrl());
    if(configuration.hasPortalUrl()) builder.setPortalUrl(configuration.getPortalUrl());
    if(configuration.hasUserAttributes())
      configuration.getUserAttributes().forEach(c -> builder.addUserAttributes(asDto(c)));
    if(configuration.getAgateVersion() != null) {
      builder.setVersion(configuration.getAgateVersion().toString());
    }
    if(configuration.hasStyle()) builder.setStyle(configuration.getStyle());
    if(configuration.hasTranslations()) builder.addAllTranslations(localizedStringDtos.asDto(configuration.getTranslations()));
    return builder.build();
  }

  @NotNull
  Configuration fromDto(@NotNull Agate.ConfigurationDtoOrBuilder dto) {
    Configuration configuration = new Configuration();
    configuration.setName(dto.getName());
    if(dto.hasDomain()) configuration.setDomain(dto.getDomain());
    if(dto.hasPublicUrl()) configuration.setPublicUrl(dto.getPublicUrl());
    if(dto.hasPortalUrl()) configuration.setPortalUrl(dto.getPortalUrl());
    dto.getLanguagesList().forEach(lang -> configuration.getLocales().add(LocaleUtils.toLocale(lang)));
    configuration.setShortTimeout(dto.getShortTimeout());
    configuration.setLongTimeout(dto.getLongTimeout());
    configuration.setInactiveTimeout(dto.getInactiveTimeout());
    configuration.setJoinPageEnabled(dto.getJoinPageEnabled());
    configuration.setJoinWithUsername(dto.getJoinWithUsername());
    if(dto.getUserAttributesCount() > 0)
      dto.getUserAttributesList().forEach(c -> configuration.addUserAttribute(fromDto(c)));
    if(dto.hasStyle()) configuration.setStyle(dto.getStyle());
    if(dto.getTranslationsCount() > 0) configuration.setTranslations(localizedStringDtos.fromDto(dto.getTranslationsList()));
    return configuration;
  }

  @NotNull
  Agate.AttributeConfigurationDto.Builder asDto(AttributeConfiguration config) {
    Agate.AttributeConfigurationDto.Builder builder = Agate.AttributeConfigurationDto.newBuilder()
      .setName(config.getName()).setRequired(config.isRequired()).setType(config.getType().toString())
      .addAllValues(config.getValues());

    if(config.hasDescription()) {
      builder.setDescription(config.getDescription());
    }

    return builder;
  }

  @NotNull
  private AttributeConfiguration fromDto(Agate.AttributeConfigurationDto config) {
    AttributeConfiguration attributeConfiguration = new AttributeConfiguration();
    attributeConfiguration.setName(config.getName());
    attributeConfiguration.setType(config.getType());
    attributeConfiguration.setRequired(config.getRequired());
    attributeConfiguration.setValues(config.getValuesList());
    attributeConfiguration.setDescription(config.hasDescription() ? config.getDescription() : null);
    return attributeConfiguration;
  }

}
