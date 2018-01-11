/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.application;

import javax.inject.Inject;

import org.obiba.agate.domain.Configuration;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.web.model.Dtos;
import org.obiba.agate.web.rest.security.AuthorizationValidator;

public class ApplicationAwareResource {

  @Inject
  private ConfigurationService configurationService;

  @Inject
  protected Dtos dtos;

  @Inject
  protected AuthorizationValidator authorizationValidator;

  private String applicationName;

  /**
   * Check application credentials.
   *
   * @param appAuthHeader
   */
  protected void validateApplication(String appAuthHeader) {
    applicationName = authorizationValidator.validateApplication(appAuthHeader);
  }

  protected String getApplicationName() {
    return applicationName;
  }

  protected Configuration getConfiguration() {
    return configurationService.getConfiguration();
  }

}
