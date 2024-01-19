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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.security.AgateTokenRealm;
import org.obiba.agate.security.Roles;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.TicketService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.rest.ticket.TicketsResource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.function.Consumer;

@Component
@Path("/auth/session/_current")
public class CurrentSessionResource {

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private TicketService ticketService;

  @DELETE
  public Response deleteSession() {
    // Delete the Shiro session
    try {
      Subject subject = SecurityUtils.getSubject();
      if (subject.isAuthenticated()) {
        Collection principals = subject.getPrincipals().fromRealm(AgateRealm.AGATE_TOKEN_REALM.getName());
        for (Object principal : principals) {
          try {
            ticketService.delete(principal.toString());
          } catch (Exception e) {
            // ignore
          }
        }
      }
      subject.logout();
    } catch (InvalidSessionException e) {
      // Ignore
    }
    Configuration configuration = getConfiguration();
    return Response.noContent().header(HttpHeaders.SET_COOKIE,
        new NewCookie(TicketsResource.TICKET_COOKIE_NAME, null, "/", configuration.getDomain(),
            "Obiba session deleted", 0, true, true)).build();
  }

  @GET
  public Agate.SessionDto get() {
    Subject subject = SecurityUtils.getSubject();
    Agate.SessionDto.Builder builder = Agate.SessionDto.newBuilder() //
      .setUsername(subject.getPrincipal().toString()) //
      .setRealm(subject.getPrincipals().getRealmNames().iterator().next());

    try {
      subject.checkRole(Roles.AGATE_ADMIN.toString());
      builder.setRole(Roles.AGATE_ADMIN.toString());
    } catch (AuthorizationException e) {
      builder.setRole(Roles.AGATE_USER.toString());
    }

    return builder.build();
  }

  @GET
  @Path("/username")
  public Response getSubject() {
    // Find the Shiro username
    return Response.ok(SecurityUtils.getSubject().getPrincipal().toString()).build();
  }

  private Configuration getConfiguration() {
    return configurationService.getConfiguration();
  }
}

