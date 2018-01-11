/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.upgrade;

import java.util.Optional;

import javax.inject.Inject;

import org.obiba.agate.domain.Configuration;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.VersionModifier;
import org.springframework.stereotype.Component;

@Component
public class AgateVersionModifier implements VersionModifier {

  @Inject
  private ConfigurationService configurationService;

  @Override
  public Version getVersion() {
    return Optional.ofNullable(configurationService.getConfiguration().getAgateVersion()).orElse(new Version(0, 9));
  }

  @Override
  public void setVersion(Version version) {
    Configuration config = configurationService.getConfiguration();
    config.setAgateVersion(version);

    configurationService.save(config);
  }
}
