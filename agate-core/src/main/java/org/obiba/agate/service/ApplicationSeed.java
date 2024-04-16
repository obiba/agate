/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import org.obiba.agate.config.Constants;
import org.obiba.agate.domain.Application;
import org.obiba.agate.domain.Configuration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ApplicationSeed implements ApplicationListener<ContextRefreshedEvent> {

  private final ConfigurationService configurationService;

  private final ApplicationService applicationService;

  private final Environment env;

  @Inject
  public ApplicationSeed(ConfigurationService configurationService, ApplicationService applicationService, Environment env) {
    this.configurationService = configurationService;
    this.applicationService = applicationService;
    this.env = env;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    Configuration config = configurationService.getConfiguration();
    if (!isDevProfile() && config.isApplicationsSeeded()) return;
    save(Application.newBuilder().name("Opal").description("The data storage application.")
        .key(applicationService.hashKey("changeit")).redirectURI("https://localhost:8443").build());
    save(Application.newBuilder().name("Mica").description("The study catalogue application.")
        .key(applicationService.hashKey("changeit")).redirectURI("https://localhost:8445").build());
    config.setApplicationsSeeded(true);
    configurationService.save(config);
  }

  private boolean isDevProfile() {
    return Lists.newArrayList(env.getActiveProfiles()).contains(Constants.SPRING_PROFILE_DEVELOPMENT);
  }

  private void save(Application application) {
    if(applicationService.findByName(application.getName().toLowerCase()) == null &&
        applicationService.findByName(application.getName()) == null) {
      try {
        applicationService.save(application);
      } catch(Exception e) {
        // ignore
      }
    }
  }
}
