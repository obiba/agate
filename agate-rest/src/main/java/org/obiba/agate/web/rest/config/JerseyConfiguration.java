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
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.obiba.agate.config.Constants;
import org.obiba.agate.web.rest.security.*;
import org.obiba.jersey.protobuf.ProtobufJsonProvider;
import org.obiba.jersey.protobuf.ProtobufNativeProvider;
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
    // Explicitly register the protobuf message body providers so they are treated as "custom" providers.
    // Since Jersey 3.1.12 / Spring Boot 4.1, the auto-discovered Jackson provider (DefaultJacksonJaxbJsonProvider)
    // and the package-scanned ProtobufJsonProvider both declare MessageBodyWriter<Object> for application/json,
    // so Jersey's WorkerComparator breaks the tie on the isCustom() flag. Registering the protobuf providers here
    // marks them custom, ensuring they win over the (non-custom) Jackson provider for protobuf Message types, while
    // Jackson still handles plain POJO/Map JSON responses. Without this, serializing protobuf DTOs to JSON fails with
    // "Direct self-reference leading to cycle" (protobuf UnknownFieldSet), surfacing as HTTP 400 on the admin UI.
    register(ProtobufJsonProvider.class);
    register(ProtobufNativeProvider.class);
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
