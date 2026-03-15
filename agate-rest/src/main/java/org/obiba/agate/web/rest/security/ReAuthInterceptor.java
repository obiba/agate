/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.obiba.agate.config.ReAuthConfiguration;
import org.obiba.web.model.ErrorDtos;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * JAX-RS request filter that checks whether the current session requires re-authentication
 * <em>before</em> the REST resource method is invoked. When re-authentication is required the
 * request is aborted with HTTP 401 and an appropriate error body, so the resource method is
 * never executed.
 */
@Component
@Priority(Priorities.AUTHENTICATION)
public class ReAuthInterceptor implements ContainerRequestFilter {

  private final ReAuthConfiguration reAuthConfiguration;

  @Inject
  public ReAuthInterceptor(ReAuthConfiguration reAuthConfiguration) {
    this.reAuthConfiguration = reAuthConfiguration;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    if (!needsReauthenticateSubject(requestContext)) return;

    ErrorDtos.ClientErrorDto errorDto = ErrorDtos.ClientErrorDto.newBuilder()
        .setCode(401)
        .setMessageTemplate("server.error.reauthentication_required")
        .setMessage("Re-authentication is required to perform this operation")
        .build();

    requestContext.abortWith(
        Response.status(Response.Status.UNAUTHORIZED)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(errorDto)
            .build()
    );
  }

  private boolean needsReauthenticateSubject(ContainerRequestContext requestContext) {
    String requestPath = requestContext.getUriInfo().getPath();
    requestPath = requestPath.startsWith("/") ? requestPath : "/" + requestPath;
    if (!reAuthConfiguration.appliesTo(requestContext.getMethod(), requestPath))
      return false;

    Subject subject = SecurityUtils.getSubject();
    if (subject == null || !subject.isAuthenticated())
      return false;

    Session session = subject.getSession(false);
    if (session == null)
      return false;

    Date startDate = session.getStartTimestamp();
    long elapsed = System.currentTimeMillis() - startDate.getTime();
    long timeoutMillis = reAuthConfiguration.getTimeout() * 1000L;
    return elapsed >= timeoutMillis;
  }
}

