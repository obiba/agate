/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.config;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.internal.InternalProperties;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.base.JsonMappingExceptionMapper;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.base.JsonParseExceptionMapper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.obiba.agate.config.Constants;
import org.obiba.agate.web.rest.security.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import jakarta.inject.Inject;

@Configuration
@ApplicationPath(JerseyConfiguration.WS_ROOT)
public class JerseyConfiguration extends ResourceConfig {

  public static final String WS_ROOT = "/ws";

  @Inject
  public JerseyConfiguration(Environment environment, CSRFTokenHelper csrfTokenHelper) {
    register(RequestContextFilter.class);
    packages("org.obiba.agate.web", "org.obiba.jersey");
    // Opt out of JacksonFeature's auto-registered DefaultJacksonJaxbJsonProvider and use AgateJacksonJsonProvider
    // instead: it does the same job but declines protobuf messages, which are the ProtobufJsonProvider's business.
    // Both providers declare MessageBodyWriter<Object> for application/json, so Jersey cannot order them by type or
    // media type distance and picks whichever comes first in an unordered set. When Jackson wins, writing a protobuf
    // DTO fails with "Direct self-reference leading to cycle" (UnknownFieldSet) and the response turns into an error.
    property(InternalProperties.JSON_FEATURE, AgateJacksonJsonProvider.class.getSimpleName());
    register(AgateJacksonJsonProvider.class);
    // exception mappers JacksonFeature would have registered along with its provider
    register(JsonParseExceptionMapper.class);
    register(JsonMappingExceptionMapper.class);
    register(ReAuthInterceptor.class);
    register(AuthenticationInterceptor.class);
    register(AuditInterceptor.class);
    register(new CSRFInterceptor(
        environment.acceptsProfiles(Profiles.of(Constants.SPRING_PROFILE_PRODUCTION)),
        environment.getProperty("csrf.allowed", ""),
        environment.getProperty("csrf.allowed-agents", ""),
        csrfTokenHelper));
    // validation errors will be sent to the client
    property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
  }

  private String getServerPort(Environment environment) {
    return environment.getProperty("server.port", "8081");
  }
}
