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

import org.obiba.agate.security.OidcAuthConfigurationProvider;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/auth/providers")
public class OidcProvidersResource {

  final OidcAuthConfigurationProvider oidcAuthConfigurationProvider;
  final Dtos dtos;

  @Inject
  public OidcProvidersResource(OidcAuthConfigurationProvider oidcAuthConfigurationProvider, Dtos dtos) {
    this.oidcAuthConfigurationProvider = oidcAuthConfigurationProvider;
    this.dtos = dtos;
  }

  @GET
  public List<Agate.OidcAuthProviderSummaryDto> getProviders(@QueryParam("locale") @DefaultValue("en") String locale) {
    return oidcAuthConfigurationProvider.getConfigurations()
      .stream()
      .map(configuration -> dtos.asSummaryDto(configuration, locale))
      .collect(Collectors.toList());
  }
}
