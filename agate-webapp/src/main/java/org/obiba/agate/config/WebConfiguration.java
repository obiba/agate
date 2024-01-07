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
import com.google.common.base.Strings;
import io.dropwizard.metrics.servlet.InstrumentedFilter;
import io.dropwizard.metrics.servlets.MetricsServlet;
import jakarta.servlet.ServletContext;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.obiba.agate.oidc.OIDCConfigurationFilter;
import org.obiba.agate.security.OidcAuthConfigurationProvider;
import org.obiba.agate.service.*;
import org.obiba.agate.web.filter.*;
import org.obiba.agate.web.filter.auth.oidc.AgateCallbackFilter;
import org.obiba.agate.web.filter.auth.oidc.AgateSignInFilter;
import org.obiba.agate.web.filter.auth.oidc.AgateSignUpFilter;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.oidc.OIDCSessionManager;
import org.obiba.shiro.web.filter.AuthenticationExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Configuration
@ComponentScan({"org.obiba.agate", "org.obiba.shiro"})
@PropertySource("classpath:agate-webapp.properties")
@AutoConfigureAfter(SecurityConfiguration.class)
public class WebConfiguration implements ServletContextInitializer, JettyServerCustomizer, EnvironmentAware {

  private static final Logger log = LoggerFactory.getLogger(WebConfiguration.class);

  private static final int DEFAULT_HTTPS_PORT = 8444;

  private static final int MAX_IDLE_TIME = 30000;

  private Environment environment;

  private final org.obiba.ssl.SslContextFactory sslContextFactory;

  private int httpsPort;

  private String serverAddress;

  private String contextPath;

  @Inject
  public WebConfiguration(org.obiba.ssl.SslContextFactory sslContextFactory) {
    this.sslContextFactory = sslContextFactory;
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
    httpsPort = environment.getProperty("https.port", Integer.class, DEFAULT_HTTPS_PORT);
    serverAddress = environment.getProperty("server.address", "localhost");
    contextPath = environment.getProperty("server.context-path", "");
    if (Strings.isNullOrEmpty(contextPath))
      contextPath = environment.getProperty("server.servlet.context-path", "");
  }

  @Bean
  public WebServerFactoryCustomizer<JettyServletWebServerFactory> containerCustomizer() throws Exception {
    return factory -> {
      factory.setServerCustomizers(Collections.singleton(WebConfiguration.this)); // FIXME is this necessary?
      if (!Strings.isNullOrEmpty(contextPath) && contextPath.startsWith("/"))
        factory.setContextPath(contextPath);
    };
  }

  @Override
  public void customize(Server server) {
    customizeSsl(server);

    GzipHandler gzipHandler = new GzipHandler();
    gzipHandler.setIncludedMethods("PUT", "POST", "GET");
    gzipHandler.setInflateBufferSize(2048);
    gzipHandler.setHandler(server.getHandler());
    server.setHandler(gzipHandler);
  }

  private void customizeSsl(Server server) {
    if (httpsPort <= 0) return;
    
    SslContextFactory.Server jettySsl = new SslContextFactory.Server() {

      @Override
      protected void doStart() throws Exception {
        setSslContext(sslContextFactory.createSslContext());
        super.doStart();
      }
    };
    jettySsl.setWantClientAuth(true);
    jettySsl.setNeedClientAuth(false);
    jettySsl.addExcludeProtocols("SSL", "SSLv2", "SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1");

    ServerConnector sslConnector = new ServerConnector(server, jettySsl);
    sslConnector.setHost(serverAddress);
    sslConnector.setPort(httpsPort);
    sslConnector.setIdleTimeout(MAX_IDLE_TIME);

    server.addConnector(sslConnector);
  }

  @Override
  public void onStartup(ServletContext servletContext) {
    log.info("Web application configuration, using profiles: {}", Arrays.toString(environment.getActiveProfiles()));

    servletContext.addListener(EnvironmentLoaderListener.class);

    // Note: authentication filter was already added by Spring

    log.info("Web application fully configured");
  }

  @Bean
  public FilterRegistrationBean<InstrumentedFilter> instrumentedFilterRegistration() {
    log.debug("Registering Instrumented Filter");
    FilterRegistrationBean<InstrumentedFilter> bean = new FilterRegistrationBean<>();

    bean.setFilter(new InstrumentedFilter());
    bean.addUrlPatterns("/*");
    bean.setAsyncSupported(true);

    return bean;
  }

  @Bean
  public ServletRegistrationBean<MetricsServlet> metricsServletRegistration(MetricRegistry metricRegistry) {
    log.debug("Registering Metrics Servlet");
    ServletRegistrationBean<MetricsServlet> bean = new ServletRegistrationBean<>();

    bean.setServlet(new MetricsServlet(metricRegistry));
    bean.addUrlMappings("/metrics/metrics/*");
    bean.setAsyncSupported(true);
    bean.setLoadOnStartup(2);

    return bean;
  }

  @Bean
  public FilterRegistrationBean<OIDCConfigurationFilter> oidcConfigurationFilterRegistration(TokenUtils tokenUtils, ConfigurationService configurationService) {
    log.debug("Registering OIDC Configuration Filter");
    FilterRegistrationBean<OIDCConfigurationFilter> bean = new FilterRegistrationBean<>();

    bean.setFilter(new OIDCConfigurationFilter(tokenUtils, configurationService));
    bean.addUrlPatterns("/.well-known/openid-configuration");
    bean.setAsyncSupported(true);

    return bean;
  }

  @Bean
  public FilterRegistrationBean<ClickjackingHttpHeadersFilter> clickjackingHttpHeadersFilterRegistration() {
    log.debug("Registering Click Jacking Http Header Filter");
    FilterRegistrationBean<ClickjackingHttpHeadersFilter> bean = new FilterRegistrationBean<>();

    bean.setFilter(new ClickjackingHttpHeadersFilter());
    bean.addUrlPatterns("/*");
    bean.setAsyncSupported(true);

    return bean;
  }

  @Bean
  @Profile(Constants.SPRING_PROFILE_PRODUCTION)
  public FilterRegistrationBean<StaticResourcesProductionFilter> staticResourcesProductionFilterRegistration() {
    log.debug("Registering Static Resources Production Filter");
    FilterRegistrationBean<StaticResourcesProductionFilter> bean = new FilterRegistrationBean<>();

    bean.setFilter(new StaticResourcesProductionFilter());
    bean.addUrlPatterns("/favicon.ico");
    bean.addUrlPatterns("/robots.txt");
    bean.addUrlPatterns("/index.html");
    bean.addUrlPatterns("/images/*");
    bean.addUrlPatterns("/fonts/*");
    bean.addUrlPatterns("/scripts/*");
    bean.addUrlPatterns("/styles/*");
    bean.addUrlPatterns("/views/*");
    bean.setAsyncSupported(true);

    return bean;
  }

  @Bean
  @Profile(Constants.SPRING_PROFILE_PRODUCTION)
  public FilterRegistrationBean<CachingHttpHeadersFilter> cachingHttpHeadersFilterRegistration() {
    log.debug("Registering Caching Htpp Headers Filter");
    FilterRegistrationBean<CachingHttpHeadersFilter> bean = new FilterRegistrationBean<>();

    bean.setFilter(new CachingHttpHeadersFilter());
    bean.addUrlPatterns("/images/*");
    bean.addUrlPatterns("/fonts/*");
    bean.addUrlPatterns("/scripts/*");
    bean.addUrlPatterns("/styles/*");
    bean.setAsyncSupported(true);

    return bean;
  }

  @Bean
  public FilterRegistrationBean<AgateCallbackFilter> agateCallbackFilterRegistration(OIDCConfigurationProvider oidcConfigurationProvider,
                                                                                     OidcAuthConfigurationProvider oidcAuthConfigurationProvider,
                                                                                     OIDCSessionManager oidcSessionManager,
                                                                                     AuthenticationExecutor authenticationExecutor,
                                                                                     ConfigurationService configurationService,
                                                                                     ApplicationService applicationService,
                                                                                     RealmConfigService realmConfigService, UserService userService,
                                                                                     TicketService ticketService,
                                                                                     TokenUtils tokenUtils) {
    log.debug("Registering Callback Filter");
    FilterRegistrationBean<AgateCallbackFilter> bean = new FilterRegistrationBean<>();

    bean.setFilter(new AgateCallbackFilter(oidcConfigurationProvider, oidcAuthConfigurationProvider, oidcSessionManager, authenticationExecutor, configurationService, applicationService, realmConfigService, userService, ticketService, tokenUtils));
    bean.addUrlPatterns("/auth/callback/*");
    bean.setAsyncSupported(true);

    return bean;
  }

  @Bean
  public FilterRegistrationBean<AgateSignInFilter> agateSignInFilterRegistration(ConfigurationService configurationService,
                                                                                 OIDCConfigurationProvider oidcConfigurationProvider,
                                                                                 OIDCSessionManager oidcSessionManager,
                                                                                 RealmConfigService realmConfigService) {
    log.debug("Registering Sign-in Filter");
    FilterRegistrationBean<AgateSignInFilter> bean = new FilterRegistrationBean<>();

    bean.setFilter(new AgateSignInFilter(configurationService, oidcConfigurationProvider, oidcSessionManager, realmConfigService));
    bean.addUrlPatterns("/auth/signin/*");
    bean.setAsyncSupported(true);

    return bean;
  }

  @Bean
  public FilterRegistrationBean<AgateSignUpFilter> agateSignUpFilterRegistration(ConfigurationService configurationService,
                                                                                 OIDCConfigurationProvider oidcConfigurationProvider,
                                                                                 OIDCSessionManager oidcSessionManager,
                                                                                 RealmConfigService realmConfigService) {
    log.debug("Registering Sign-up Filter");
    FilterRegistrationBean<AgateSignUpFilter> bean = new FilterRegistrationBean<>();

    bean.setFilter(new AgateSignUpFilter(configurationService, oidcConfigurationProvider, oidcSessionManager, realmConfigService));
    bean.addUrlPatterns("/auth/signup/*");
    bean.setAsyncSupported(true);

    return bean;
  }

  @Bean
  public FilterRegistrationBean<NoTraceFilter> noTraceFilterRegistration() {
    log.debug("Registering No Trace Filter");
    FilterRegistrationBean<NoTraceFilter> bean = new FilterRegistrationBean<>();

    bean.setFilter(new NoTraceFilter());
    bean.addUrlPatterns("/*");
    bean.setAsyncSupported(true);

    return bean;
  }

  @Bean
  public FilterRegistrationBean<ForbiddenUrlsFilter> forbiddenUrlsFilterRegistration() {
    log.debug("Registering Forbidden Urls Filter");
    FilterRegistrationBean<ForbiddenUrlsFilter> bean = new FilterRegistrationBean<>();

    bean.setFilter(new ForbiddenUrlsFilter());
    bean.addUrlPatterns("/.htaccess");
    bean.addUrlPatterns("/.htaccess/");
    bean.setAsyncSupported(true);

    return bean;
  }

}
