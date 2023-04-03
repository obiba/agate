/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate;

import org.obiba.agate.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * This is an helper Java class that provides an alternative to creating a web.xml.
 */
public class ApplicationWebXml extends SpringBootServletInitializer {

  private static final Logger log = LoggerFactory.getLogger(ApplicationWebXml.class);

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.profiles(addDefaultProfile()).bannerMode(Banner.Mode.OFF).sources(Application.class);
  }

  /**
   * Set a default profile if it has not been set.
   * <p>
   * Please use -Dspring.profiles.active=prod
   * </p>
   */
  private String addDefaultProfile() {
    String profile = System.getProperty("spring.profiles.active");
    if(profile != null) {
      log.info("Running with Spring profile(s) : {}", profile);
      return profile;
    }

    log.warn("No Spring profile configured, running with default configuration");
    return Constants.SPRING_PROFILE_PRODUCTION;
  }
}
