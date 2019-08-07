/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.security;

import com.google.common.base.Strings;
import org.obiba.agate.domain.RealmUsage;
import org.obiba.agate.security.OidcAuthConfigurationProvider;
import org.obiba.agate.web.model.Dtos;
import org.obiba.shiro.realm.ObibaRealm;
import org.obiba.web.model.OIDCDtos;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/auth/providers")
public class OidcProvidersResource {

  private final OidcAuthConfigurationProvider oidcAuthConfigurationProvider;

  private final Dtos dtos;

  private final AuthorizationValidator authorizationValidator;

  @Inject
  public OidcProvidersResource(OidcAuthConfigurationProvider oidcAuthConfigurationProvider,
                               Dtos dtos,
                               AuthorizationValidator authorizationValidator) {
    this.oidcAuthConfigurationProvider = oidcAuthConfigurationProvider;
    this.dtos = dtos;
    this.authorizationValidator = authorizationValidator;
  }

  @GET
  public List<OIDCDtos.OIDCAuthProviderSummaryDto> getProviders(@QueryParam("locale") @DefaultValue("en") String locale,
                                                                @QueryParam("usage") @DefaultValue("ALL") RealmUsage usage,
                                                                @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader) {

    if (Strings.isNullOrEmpty(authHeader)) {
      // Agate application
      return oidcAuthConfigurationProvider.getConfigurations(usage)
        .stream()
        .map(configuration -> dtos.asSummaryDto(configuration, locale))
        .collect(Collectors.toList());
    }

    String application = authorizationValidator.validateApplication(authHeader);
    return oidcAuthConfigurationProvider.getConfigurationsForApplication(usage, application)
      .stream()
      .map(configuration -> dtos.asSummaryDto(configuration, locale))
      .collect(Collectors.toList());
  }
}
