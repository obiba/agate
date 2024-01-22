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

import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

@Configuration("cacheConfiguration")
@EnableCaching
@AutoConfigureAfter(value = { MetricsConfiguration.class })
public class CacheConfiguration implements DisposableBean {

  private static final Logger log = LoggerFactory.getLogger(CacheConfiguration.class);

  private final MetricRegistry metricRegistry;

  @Inject
  public CacheConfiguration(MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
  }

  @Override
  public void destroy() {
    log.info("Remove Cache Manager metrics");
    metricRegistry.getNames().forEach(metricRegistry::remove);
  }
}
