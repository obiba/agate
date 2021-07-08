/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.agate.security;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.domain.*;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Get only the "enabled" OpenID Connect configurations.
 */
@Component
public class OidcAuthConfigurationProvider implements OIDCConfigurationProvider {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ConfigurationService configurationService;

  private final RealmConfigService realmConfigService;

  @Inject
  public OidcAuthConfigurationProvider(ConfigurationService configurationService, RealmConfigService realmConfigService) {
    this.configurationService = configurationService;
    this.realmConfigService = realmConfigService;
  }

  @Override
  public Collection<OIDCConfiguration> getConfigurations() {
    return getConfigurations(RealmUsage.ALL);
  }

  public Collection<OIDCConfiguration> getConfigurations(RealmUsage usage) {
    return realmConfigService.findAllForUsageByStatusAndType(usage, RealmStatus.ACTIVE, AgateRealm.AGATE_OIDC_REALM)
      .stream()
      .map(realm -> createOIDCConfiguration(realm))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  public Collection<OIDCConfiguration> getConfigurationsForApplication(RealmUsage usage, String application) {
    return realmConfigService
      .findAllForUsageByStatusAndTypeAndApplication(usage, RealmStatus.ACTIVE, AgateRealm.AGATE_OIDC_REALM, application)
        .stream()
        .map(realm -> createOIDCConfiguration(realm))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public OIDCConfiguration getConfiguration(String name) {
    if (Strings.isNullOrEmpty(name)) return null;
    return getConfigurations(RealmUsage.ALL).stream().filter(conf -> name.equals(conf.getName())).findFirst().orElse(null);
  }

  /**
   * Add the Realm name as the provider name for authentication purposes. Use the Realm title as a custom parameter for
   * later use by the client applications.
   *
   * @param realmConfig
   * @return
   */
  private OIDCConfiguration createOIDCConfiguration(RealmConfig realmConfig) {
    String name = realmConfig.getName();
    LocalizedString title = realmConfig.getTitle();
    String content = realmConfig.getContent();
    try {
      OidcRealmConfig oidcRealmConfig = OidcRealmConfig.newBuilder(configurationService.decrypt(content))
          .setUserInfoMapping(realmConfig.getUserInfoMapping()).build();
      oidcRealmConfig.setName(name);
      Map<String, String> valueMap = Maps.newHashMap();

      // Add localized title as custom parameters
      configurationService.getConfiguration()
        .getLocales()
        .forEach(locale -> {
          String language = locale.getLanguage();
          String value = title == null ? name : title.get(language);
          if (!Strings.isNullOrEmpty(value)) valueMap.put(language, value);
        });

      if (valueMap.size() > 0) {
        oidcRealmConfig.setCustomParam("title", new JSONObject(valueMap).toString());
      }

      return oidcRealmConfig;
    } catch (JSONException e) {
      logger.error(e.getMessage());
    }

    return null;
  }
}
