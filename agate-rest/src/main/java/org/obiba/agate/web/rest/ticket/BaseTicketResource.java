/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.ticket;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import org.obiba.agate.domain.Configuration;
import org.obiba.agate.service.ApplicationService;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.web.model.Dtos;
import org.obiba.shiro.authc.HttpAuthorizationToken;
import org.obiba.shiro.realm.ObibaRealm;

public class BaseTicketResource {

  @Inject
  private ApplicationService applicationService;

  @Inject
  private ConfigurationService configurationService;

  @Inject
  protected Dtos dtos;

  private String applicationName;

  protected void validateApplication(String appAuthHeader) {
    if (appAuthHeader == null) throw new ForbiddenException();

    HttpAuthorizationToken token = new HttpAuthorizationToken(ObibaRealm.APPLICATION_AUTH_SCHEMA, appAuthHeader);
    validateApplicationParameters(token.getUsername(), new String(token.getPassword()));
  }

  private void validateApplicationParameters(String name, String key) {
    if(!applicationService.isValid(name, key)) throw new ForbiddenException();
    applicationName = name;
  }

  protected String getApplicationName() {
    return applicationName;
  }

  protected Configuration getConfiguration() {
    return configurationService.getConfiguration();
  }
}
