/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="client")
public class ClientConfiguration {
  private String reCaptchaKey;

  public String getReCaptchaKey() {
    return reCaptchaKey;
  }

  public void setReCaptchaKey(String reCaptchaKey) {
    this.reCaptchaKey = reCaptchaKey;
  }
}
