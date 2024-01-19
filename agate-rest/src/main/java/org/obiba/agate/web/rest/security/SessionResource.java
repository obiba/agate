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

import jakarta.annotation.Nullable;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;
import org.springframework.stereotype.Component;

@Component
@Path("/auth/session/{id}")
public class SessionResource  {

  @HEAD
  public Response checkSession(@PathParam("id") String sessionId) {
    // Find the Shiro Session
    return isValidSessionId(sessionId) ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build();
  }

  private boolean isValidSessionId(String sessionId) {
    return getSession(sessionId) != null;
  }

  @Nullable
  private Session getSession(String sessionId) {
    if(sessionId != null) {
      SessionKey key = new DefaultSessionKey(sessionId);
      try {
        return SecurityUtils.getSecurityManager().getSession(key);
      } catch(SessionException e) {
        // Means that the session does not exist or has expired.
      }
    }
    return null;
  }
}
