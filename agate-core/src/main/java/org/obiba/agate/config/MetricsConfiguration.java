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

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.jvm.*;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.inject.Inject;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableMetrics(proxyTargetClass = true)
public class MetricsConfiguration extends MetricsConfigurerAdapter implements EnvironmentAware {

  private static final String ENV_METRICS = "metrics";

  private static final String ENV_METRICS_GRAPHITE = "metrics.graphite";

  private static final String PROP_JMX_ENABLED = "jmx.enabled";

  private static final String PROP_GRAPHITE_ENABLED = "enabled";

  private static final String PROP_PORT = "port";

  private static final String PROP_HOST = "host";

  private static final String PROP_METRIC_REG_JVM_MEMORY = "jvm.memory";

  private static final String PROP_METRIC_REG_JVM_GARBAGE = "jvm.garbage";

  private static final String PROP_METRIC_REG_JVM_THREADS = "jvm.threads";

  private static final String PROP_METRIC_REG_JVM_FILES = "jvm.files";

  private static final String PROP_METRIC_REG_JVM_BUFFERS = "jvm.buffers";

  private static final Logger log = LoggerFactory.getLogger(MetricsConfiguration.class);

  private Environment environment;

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Override
  public void configureReporters(MetricRegistry metricRegistry) {
    // registerReporter allows the MetricsConfigurerAdapter to
    // shut down the reporter when the Spring context is closed
    log.debug("Registering JVM gauges");
    metricRegistry.register(PROP_METRIC_REG_JVM_MEMORY, new MemoryUsageGaugeSet());
    metricRegistry.register(PROP_METRIC_REG_JVM_GARBAGE, new GarbageCollectorMetricSet());
    metricRegistry.register(PROP_METRIC_REG_JVM_THREADS, new ThreadStatesGaugeSet());
    metricRegistry.register(PROP_METRIC_REG_JVM_FILES, new FileDescriptorRatioGauge());
    metricRegistry
        .register(PROP_METRIC_REG_JVM_BUFFERS, new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
    if(environment.getProperty(String.format("%s.%s", ENV_METRICS, PROP_JMX_ENABLED), Boolean.class, false)) {
      log.info("Initializing Metrics JMX reporting");
      JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build();
      jmxReporter.start();
    }
  }

  @Configuration
  @ConditionalOnClass(Graphite.class)
  public static class GraphiteRegistry implements EnvironmentAware, InitializingBean {

    private final Logger log = LoggerFactory.getLogger(GraphiteRegistry.class);

    @Inject
    private MetricRegistry metricRegistry;

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
      this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() {
      Boolean graphiteEnabled = environment.getProperty(String.format("%s.%s", ENV_METRICS_GRAPHITE, PROP_GRAPHITE_ENABLED), Boolean.class, false);
      if(graphiteEnabled) {
        log.info("Initializing Metrics Graphite reporting");
        String graphiteHost = environment.getProperty(String.format("%s.%s", ENV_METRICS_GRAPHITE, PROP_HOST), "localhost");
        Integer graphitePort = environment.getProperty(String.format("%s.%s", ENV_METRICS_GRAPHITE, PROP_PORT), Integer.class, 2003);
        Graphite graphite = new Graphite(new InetSocketAddress(graphiteHost, graphitePort));
        GraphiteReporter graphiteReporter = GraphiteReporter.forRegistry(metricRegistry)
            .convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build(graphite);
        graphiteReporter.start(1, TimeUnit.MINUTES);
      }
    }
  }
}
