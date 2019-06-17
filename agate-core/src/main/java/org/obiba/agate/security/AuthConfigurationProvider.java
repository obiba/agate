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
import com.google.common.collect.Lists;
import org.json.JSONException;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.OidcRealmConfig;
import org.obiba.agate.domain.RealmStatus;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Get only the "enabled" OpenID Connect configurations.
 */
@Component
public class AuthConfigurationProvider implements OIDCConfigurationProvider {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ConfigurationService configurationService;

  private final RealmConfigService realmConfigService;

  @Inject
  public AuthConfigurationProvider(ConfigurationService configurationService, RealmConfigService realmConfigService) {
    this.configurationService = configurationService;
    this.realmConfigService = realmConfigService;
  }

  @Override
  public Collection<OIDCConfiguration> getConfigurations() {
    return realmConfigService.findAllForSignupByStatusAndType(RealmStatus.ACTIVE, AgateRealm.AGATE_OIDC_REALM)
      .stream()
      .map(realm -> createOIDCConfiguration(realm.getName(), realm.getContent()))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  @Override
  public OIDCConfiguration getConfiguration(String name) {
    if (Strings.isNullOrEmpty(name)) return null;
    return getConfigurations().stream().filter(conf -> name.equals(conf.getName())).findFirst().orElse(null);
  }

  private OIDCConfiguration createOIDCConfiguration(String name, String content) {
    try {
      OidcRealmConfig oidcRealmConfig = OidcRealmConfig.newBuilder(configurationService.decrypt(content)).build();
      oidcRealmConfig.setName(name);
      return oidcRealmConfig;
    } catch (JSONException e) {
      logger.error(e.getMessage());
      return null;
    }
  }
}
