/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.config.metrics;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * SpringBoot Actuator HealthIndicator check for the Database.
 */
public class DatabaseHealthIndicator extends HealthCheckIndicator {

  private static final Logger log = LoggerFactory.getLogger(DatabaseHealthIndicator.class);

  @Inject
  private MongoTemplate mongoTemplate;

  @Override
  protected Result check() {
    log.debug("Initializing Database health indicator");
    try {
      if(mongoTemplate.getDb().getStats().ok()) {
        return healthy();
      }
      return unhealthy("Cannot connect to database.");
    } catch(Exception e) {
      log.debug("Cannot connect to Database.", e);
      return unhealthy("Cannot connect to database.", e);
    }
  }
}
