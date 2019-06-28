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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlets.GzipFilter;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.obiba.agate.oidc.OIDCConfigurationFilter;
import org.obiba.agate.web.filter.CachingHttpHeadersFilter;
import org.obiba.agate.web.filter.StaticResourcesProductionFilter;
import org.obiba.agate.web.filter.auth.oidc.AgateCallbackFilter;
import org.obiba.agate.web.filter.auth.oidc.AgateSignInFilter;
import org.obiba.agate.web.filter.auth.oidc.AgateSignUpFilter;
import org.obiba.shiro.web.filter.AuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlet.InstrumentedFilter;
import com.codahale.metrics.servlets.MetricsServlet;

import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.ERROR;
import static javax.servlet.DispatcherType.FORWARD;
import static javax.servlet.DispatcherType.INCLUDE;
import static javax.servlet.DispatcherType.REQUEST;
import static org.obiba.agate.web.rest.config.JerseyConfiguration.WS_ROOT;

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Configuration
@ComponentScan({ "org.obiba.agate", "org.obiba.shiro" })
@PropertySource("classpath:agate-webapp.properties")
@AutoConfigureAfter(SecurityConfiguration.class)
public class WebConfiguration implements ServletContextInitializer, JettyServerCustomizer, EnvironmentAware {

  private static final Logger log = LoggerFactory.getLogger(WebConfiguration.class);

  private static final int DEFAULT_HTTPS_PORT = 8444;

  private static final int MAX_IDLE_TIME = 30000;

  private static final int REQUEST_HEADER_SIZE = 8192;

  private Environment environment;

  private final MetricRegistry metricRegistry;

  private final org.obiba.ssl.SslContextFactory sslContextFactory;

  private final AuthenticationFilter authenticationFilter;

  private final AgateSignInFilter agateSignInFilter;

  private final AgateSignUpFilter agateSignUpFilter;

  private final AgateCallbackFilter agateCallbackFilter;

  private final OIDCConfigurationFilter oidcConfigurationFilter;

  private int httpsPort;

  @Inject
  public WebConfiguration(
    MetricRegistry metricRegistry,
    org.obiba.ssl.SslContextFactory sslContextFactory,
    AuthenticationFilter authenticationFilter,
    AgateSignInFilter agateSignInFilter,
    AgateSignUpFilter agateSignUpFilter,
    AgateCallbackFilter agateCallbackFilter,
    OIDCConfigurationFilter oidcConfigurationFilter) {

    this.metricRegistry = metricRegistry;
    this.sslContextFactory = sslContextFactory;
    this.authenticationFilter = authenticationFilter;
    this.agateSignUpFilter = agateSignUpFilter;
    this.oidcConfigurationFilter = oidcConfigurationFilter;
    this.agateSignInFilter = agateSignInFilter;
    this.agateCallbackFilter = agateCallbackFilter;
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
    RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, "https.");
    httpsPort = propertyResolver.getProperty("port", Integer.class, DEFAULT_HTTPS_PORT);
  }

  @Bean
  public EmbeddedServletContainerCustomizer containerCustomizer() throws Exception {
    return (ConfigurableEmbeddedServletContainer container) -> {
      JettyEmbeddedServletContainerFactory jetty = (JettyEmbeddedServletContainerFactory) container;
      jetty.setServerCustomizers(Collections.singleton(this));
    };
  }

  @Override
  public void customize(Server server) {
    customizeSsl(server);
  }

  private void customizeSsl(Server server) {
    SslContextFactory jettySsl = new SslContextFactory() {

      @Override
      protected void doStart() throws Exception {
        setSslContext(sslContextFactory.createSslContext());
        super.doStart();
      }
    };
    jettySsl.setWantClientAuth(true);
    jettySsl.setNeedClientAuth(false);
    jettySsl.addExcludeProtocols("SSLv2", "SSLv3");

    ServerConnector sslConnector = new ServerConnector(server, jettySsl);
    sslConnector.setPort(httpsPort);
    sslConnector.setIdleTimeout(MAX_IDLE_TIME);

    server.addConnector(sslConnector);
  }

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    log.info("Web application configuration, using profiles: {}", Arrays.toString(environment.getActiveProfiles()));

    servletContext.addListener(EnvironmentLoaderListener.class);

    initAllowedMethodsFilter(servletContext);
    initAuthenticationFilter(servletContext);
    initOIDCAuthenticationFilter(servletContext);
    initOIDCConfigurationFilter(servletContext);

    EnumSet<DispatcherType> disps = EnumSet.of(REQUEST, FORWARD, ASYNC);
    initMetrics(servletContext, disps);
    if(environment.acceptsProfiles(Constants.SPRING_PROFILE_PRODUCTION)) {
      initStaticResourcesProductionFilter(servletContext, disps);
      initCachingHttpHeadersFilter(servletContext, disps);
    }
    initGzipFilter(servletContext, disps);

    log.info("Web application fully configured");
  }

  private void initAllowedMethodsFilter(ServletContext servletContext) {
    log.debug("Registering Allowed Methods Filter");

    FilterRegistration.Dynamic filterRegistration = servletContext.addFilter("noTrace", new NoTraceFilter());

    filterRegistration.addMappingForUrlPatterns(EnumSet.of(REQUEST, FORWARD, ASYNC, INCLUDE, ERROR), true, "/*");
    filterRegistration.setAsyncSupported(true);
  }

  private void initAuthenticationFilter(ServletContext servletContext) {
    log.debug("Registering Authentication Filter");
    FilterRegistration.Dynamic filterRegistration = servletContext.addFilter("authenticationFilter", authenticationFilter);

    if (filterRegistration == null) {
      filterRegistration =
        (FilterRegistration.Dynamic)servletContext.getFilterRegistration("authenticationFilter");
    }

    log.debug("Adding mapping to authentication filter registration");

    filterRegistration.addMappingForUrlPatterns(EnumSet.of(REQUEST, FORWARD, ASYNC, INCLUDE, ERROR), true,
      WS_ROOT + "/*");
    filterRegistration.setAsyncSupported(true);
  }

  private void initOIDCConfigurationFilter(ServletContext servletContext) {
    log.debug("Registering OIDC Configuration Filter");
    FilterRegistration.Dynamic filterRegistration = servletContext.addFilter("OIDCConfigurationFilter", oidcConfigurationFilter);

    if (filterRegistration == null) {
      filterRegistration =
        (FilterRegistration.Dynamic)servletContext.getFilterRegistration("OIDCConfigurationFilter");
    }

    log.debug("Adding mapping to OIDC configuration filter registration");

    filterRegistration.addMappingForUrlPatterns(EnumSet.of(REQUEST, FORWARD, ASYNC, INCLUDE, ERROR), true,"/.well-known/openid-configuration");
    filterRegistration.setAsyncSupported(true);
  }

  private void initOIDCAuthenticationFilter(ServletContext servletContext) {
    log.debug("Registering OIDC Authentication Filter");
    FilterRegistration.Dynamic signInFilterRegistration = servletContext.addFilter("agateSignInFilter", agateSignInFilter);
    signInFilterRegistration.addMappingForUrlPatterns(EnumSet.of(REQUEST, FORWARD, INCLUDE, ERROR), true, "/auth/signin/*");

    FilterRegistration.Dynamic signUpFilterRegistration = servletContext.addFilter("agateSignUpFilter", agateSignUpFilter);
    signUpFilterRegistration.addMappingForUrlPatterns(EnumSet.of(REQUEST, FORWARD, INCLUDE, ERROR), true, "/auth/signup/*");

    FilterRegistration.Dynamic callbackFilterRegistration = servletContext.addFilter("agateCallbackFilter", agateCallbackFilter);
    callbackFilterRegistration.addMappingForUrlPatterns(EnumSet.of(REQUEST, FORWARD, INCLUDE, ERROR), true, "/auth/callback/*");
  }

  /**
   * Initializes the GZip filter.
   */
  private void initGzipFilter(ServletContext servletContext, EnumSet<DispatcherType> disps) {
    log.debug("Registering GZip Filter");

    FilterRegistration.Dynamic compressingFilter = servletContext.addFilter("gzipFilter", new GzipFilter());

    if (compressingFilter == null) {
      compressingFilter = (FilterRegistration.Dynamic)servletContext.getFilterRegistration("gzipFilter");
    }

    compressingFilter.addMappingForUrlPatterns(disps, true, "*.css");
    compressingFilter.addMappingForUrlPatterns(disps, true, "*.json");
    compressingFilter.addMappingForUrlPatterns(disps, true, "*.html");
    compressingFilter.addMappingForUrlPatterns(disps, true, "*.js");
    compressingFilter.addMappingForUrlPatterns(disps, true, "/metrics/*");
    compressingFilter.addMappingForUrlPatterns(disps, true, WS_ROOT + "/*");
    compressingFilter.setAsyncSupported(true);
  }

  /**
   * Initializes the static resources production Filter.
   */
  private void initStaticResourcesProductionFilter(ServletContext servletContext, EnumSet<DispatcherType> disps) {

    log.debug("Registering static resources production Filter");
    FilterRegistration.Dynamic resourcesFilter = servletContext
        .addFilter("staticResourcesProductionFilter", new StaticResourcesProductionFilter());

    resourcesFilter.addMappingForUrlPatterns(disps, true, "/favicon.ico");
    resourcesFilter.addMappingForUrlPatterns(disps, true, "/robots.txt");
    resourcesFilter.addMappingForUrlPatterns(disps, true, "/index.html");
    resourcesFilter.addMappingForUrlPatterns(disps, true, "/images/*");
    resourcesFilter.addMappingForUrlPatterns(disps, true, "/fonts/*");
    resourcesFilter.addMappingForUrlPatterns(disps, true, "/scripts/*");
    resourcesFilter.addMappingForUrlPatterns(disps, true, "/styles/*");
    resourcesFilter.addMappingForUrlPatterns(disps, true, "/views/*");
    resourcesFilter.setAsyncSupported(true);
  }

  /**
   * Initializes the caching HTTP Headers Filter.
   */
  private void initCachingHttpHeadersFilter(ServletContext servletContext, EnumSet<DispatcherType> disps) {
    log.debug("Registering Caching HTTP Headers Filter");
    FilterRegistration.Dynamic cachingFilter = servletContext
        .addFilter("cachingHttpHeadersFilter", new CachingHttpHeadersFilter());

    cachingFilter.addMappingForUrlPatterns(disps, true, "/images/*");
    cachingFilter.addMappingForUrlPatterns(disps, true, "/fonts/*");
    cachingFilter.addMappingForUrlPatterns(disps, true, "/scripts/*");
    cachingFilter.addMappingForUrlPatterns(disps, true, "/styles/*");
    cachingFilter.setAsyncSupported(true);
  }

  /**
   * Initializes Metrics.
   */
  private void initMetrics(ServletContext servletContext, EnumSet<DispatcherType> disps) {
    log.debug("Initializing Metrics registries");
    servletContext.setAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE, metricRegistry);
    servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY, metricRegistry);

    log.debug("Registering Metrics Filter");
    FilterRegistration.Dynamic metricsFilter = servletContext
        .addFilter("webappMetricsFilter", new InstrumentedFilter());

    metricsFilter.addMappingForUrlPatterns(disps, true, "/*");
    metricsFilter.setAsyncSupported(true);

    log.debug("Registering Metrics Servlet");
    ServletRegistration.Dynamic metricsAdminServlet = servletContext.addServlet("metricsServlet", new MetricsServlet());

    metricsAdminServlet.addMapping("/metrics/metrics/*");
    metricsAdminServlet.setAsyncSupported(true);
    metricsAdminServlet.setLoadOnStartup(2);
  }

  /**
   * When a TRACE request is received, returns a Forbidden response.
   */
  private static class NoTraceFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      HttpServletResponse httpResponse = (HttpServletResponse) response;

      if("TRACE".equals(httpRequest.getMethod())) {
        httpResponse.reset();
        httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "TRACE method not allowed");
        return;
      }
      chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
  }

}
