package org.obiba.agate.web.rest.config;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.obiba.agate.web.rest.security.AuthenticationInterceptor;
import org.springframework.stereotype.Component;

@Component
@ApplicationPath(JerseyConfiguration.WS_ROOT)
public class JerseyConfiguration extends ResourceConfig {

  public static final String WS_ROOT = "/ws";

  public JerseyConfiguration() {
    register(RequestContextFilter.class);
    packages("org.obiba.agate.web", "org.obiba.jersey", "com.fasterxml.jackson");
    register(LoggingFilter.class);
    register(AuthenticationInterceptor.class);
    // validation errors will be sent to the client
    property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
  }
}
