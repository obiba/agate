/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.controller.domain;

import org.obiba.agate.config.ClientConfiguration;
import org.obiba.agate.domain.AttributeConfiguration;
import org.obiba.agate.domain.Configuration;

import java.util.List;

public class AuthConfiguration {

  private final Configuration configuration;

  private final ClientConfiguration clientConfiguration;

  public AuthConfiguration(Configuration configuration, ClientConfiguration clientConfiguration) {
    this.configuration = configuration;
    this.clientConfiguration = clientConfiguration;
  }

  public String getReCaptchaKey() {
    return clientConfiguration.getReCaptchaKey();
  }

  public boolean getJoinWithUsername() {
    return configuration.isJoinWithUsername();
  }

  public List<String> getLanguages() {
    return configuration.getLocalesAsString();
  }

  public List<AttributeConfiguration> getUserAttributes() {
    return configuration.getUserAttributes();
  }

}
