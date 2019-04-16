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

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;

@Configuration("cacheConfiguration")
@EnableCaching
@AutoConfigureAfter(value = { MetricsConfiguration.class })
public class CacheConfiguration {

  private static final Logger log = LoggerFactory.getLogger(CacheConfiguration.class);

  private final MetricRegistry metricRegistry;

  @Inject
  public CacheConfiguration(MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
  }

  @PreDestroy
  public void destroy() {
    log.info("Remove Cache Manager metrics");
    metricRegistry.getNames().forEach(metricRegistry::remove);
  }

  @Bean
  public EhCacheManagerFactoryBean cacheManagerFactory() {
    log.debug("Starting Ehcache");
    EhCacheManagerFactoryBean factoryBean = new EhCacheManagerFactoryBean();
    factoryBean.setCacheManagerName("agate");
    return factoryBean;
  }

  @Bean
  public CacheManager springCacheManager() {
    log.debug("Starting Spring Cache");
    EhCacheCacheManager ehCacheManager = new EhCacheCacheManager();
    ehCacheManager.setCacheManager(cacheManagerFactory().getObject());
    return ehCacheManager;
  }
}
