/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.ticket;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.obiba.agate.domain.GrantingTicket;
import org.obiba.agate.service.cas.GrantingTicketService;
import org.obiba.agate.web.rest.config.JerseyConfiguration;
import org.obiba.agate.web.rest.security.SessionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@Path("/")
public class TicketsResource {

  private static final Logger log = LoggerFactory.getLogger(TicketsResource.class);

  public static final String TICKET_COOKIE_NAME = "obibaid";

  /**
   * 8 hours
   */
  private static final int SHORT_TERM_TICKET_TIMEOUT = 8 * 3600;

  /**
   * 3 months
   */
  private static final int LONG_TERM_TICKET_TIMEOUT = 3 * 30 * 24 * 3600;

  @Inject
  private GrantingTicketService grantingTicketService;

  @POST
  @Path("/login")
  public Response login(@SuppressWarnings("TypeMayBeWeakened") @Context HttpServletRequest servletRequest,
      @QueryParam("rememberMe") @DefaultValue("false") boolean rememberMe,
      @QueryParam("renew") @DefaultValue("false") boolean renew, @FormParam("username") String username,
      @FormParam("password") String password) {
    try {
      Subject subject = SecurityUtils.getSubject();
      subject.login(new UsernamePasswordToken(username, password));
      subject.logout();

      GrantingTicket ticket;
      List<GrantingTicket> tickets = grantingTicketService.findByUsername(username);
      if (renew) grantingTicketService.deleteAll(tickets);
      if(renew || tickets == null || tickets.isEmpty()) {
        ticket = new GrantingTicket();
        ticket.setUsername(username);
      } else {
        ticket = tickets.get(0);
      }
      ticket.setRemembered(rememberMe);
      grantingTicketService.save(ticket);
      NewCookie cookie = new NewCookie(TICKET_COOKIE_NAME, ticket.getCASId(), "/", null, null,
          rememberMe ? LONG_TERM_TICKET_TIMEOUT : SHORT_TERM_TICKET_TIMEOUT, false);
      log.info("Successful Granting Ticket creation for user '{}' with CAS ID: {}", username, ticket.getCASId());
      return Response.created(
          UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(SessionResource.class).build(ticket.getCASId()))
          .header(HttpHeaders.SET_COOKIE, cookie).build();

    } catch(AuthenticationException e) {
      log.info("Authentication failure of user '{}' at ip: '{}': {}", username, servletRequest.getRemoteAddr(),
          e.getMessage());
      // When a request contains credentials and they are invalid, the a 403 (Forbidden) should be returned.
      return Response.status(Response.Status.FORBIDDEN).cookie().build();
    }
  }

  @GET
  @Path("/validate")
  public Response validate(@QueryParam("ticket") String ticket) {
    GrantingTicket grantingTicket = grantingTicketService.findById(ticket);
    return Response.ok().entity(grantingTicket.getUsername()).build();
  }

}

