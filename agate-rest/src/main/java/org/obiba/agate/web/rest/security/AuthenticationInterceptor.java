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

import java.io.IOException;

import jakarta.annotation.Priority;
import javax.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.obiba.agate.service.ConfigurationService;
import org.springframework.stereotype.Component;

@Component
@Priority(Integer.MIN_VALUE)
public class AuthenticationInterceptor implements ContainerResponseFilter {

  private static final String AGATE_SESSION_ID_COOKIE_NAME = "agatesid";

  @Inject
  private ConfigurationService configurationService;

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    // Set the cookie if the user is still authenticated
    String path = configurationService.getContextPath() + "/";
    if(isUserAuthenticated()) {
      Session session = SecurityUtils.getSubject().getSession();
      session.touch();
      int timeout = (int) (session.getTimeout() / 1000);
      responseContext.getHeaders().add(HttpHeaders.SET_COOKIE,
          new NewCookie(AGATE_SESSION_ID_COOKIE_NAME, session.getId().toString(), path, null, null, timeout, true, true));
    } else {
      if(responseContext.getHeaders().get(HttpHeaders.SET_COOKIE) == null) {
        responseContext.getHeaders().putSingle(HttpHeaders.SET_COOKIE,
            new NewCookie(AGATE_SESSION_ID_COOKIE_NAME, null, path, null, "Agate session deleted", 0, true, true));
      }
    }
  }

  private boolean isUserAuthenticated() {
    return SecurityUtils.getSubject().isAuthenticated();
  }

}
