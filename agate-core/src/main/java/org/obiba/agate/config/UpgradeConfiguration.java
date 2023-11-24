/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.config;

import org.obiba.agate.upgrade.AgateVersionModifier;
import org.obiba.agate.upgrade.RuntimeVersionProvider;
import org.obiba.runtime.upgrade.UpgradeManager;
import org.obiba.runtime.upgrade.UpgradeStep;
import org.obiba.runtime.upgrade.support.DefaultUpgradeManager;
import org.obiba.runtime.upgrade.support.NullVersionNewInstallationDetectionStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class UpgradeConfiguration {

  @Bean
  public UpgradeManager upgradeManager(AgateVersionModifier agateVersionModifier,
    RuntimeVersionProvider runtimeVersionProvider, List<UpgradeStep> upgradeSteps) {
    DefaultUpgradeManager upgradeManager = new DefaultUpgradeManager();
    upgradeManager.setUpgradeSteps(upgradeSteps);
    upgradeManager.setInstallSteps(new ArrayList<>());
    upgradeManager.setCurrentVersionProvider(agateVersionModifier);
    upgradeManager.setRuntimeVersionProvider(runtimeVersionProvider);

    NullVersionNewInstallationDetectionStrategy newInstallationDetectionStrategy
      = new NullVersionNewInstallationDetectionStrategy();
    newInstallationDetectionStrategy.setVersionProvider(agateVersionModifier);
    upgradeManager.setNewInstallationDetectionStrategy(newInstallationDetectionStrategy);

    return upgradeManager;
  }
}
