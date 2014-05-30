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
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.TicketService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.rest.config.JerseyConfiguration;
import org.obiba.web.model.AuthDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@Path("/tickets")
public class TicketsResource extends BaseTicketResource {

  private static final Logger log = LoggerFactory.getLogger(TicketsResource.class);

  public static final String TICKET_COOKIE_NAME = "obibaid";

  @Value("${ticket.domain}")
  private String domain;

  @Value("${ticket.timeout.short}")
  private int shortTermTicketHours;

  @Value("${ticket.timeout.long}")
  private int longTermTicketHours;

  @Inject
  private TicketService ticketService;

  @Inject
  private UserService userService;

  @POST
  public Response login(@SuppressWarnings("TypeMayBeWeakened") @Context HttpServletRequest servletRequest,
      @QueryParam("rememberMe") @DefaultValue("false") boolean rememberMe,
      @QueryParam("renew") @DefaultValue("false") boolean renew, @QueryParam("application") String application,
      @QueryParam("key") String key, @FormParam("username") String username, @FormParam("password") String password) {

    validateApplication(application, key);

    try {
      Subject subject = SecurityUtils.getSubject();
      subject.login(new UsernamePasswordToken(username, password));
      subject.logout();

      Ticket ticket = createTicket(username, renew, rememberMe, application);
      int timeout = rememberMe ? shortTermTicketHours : longTermTicketHours;
      NewCookie cookie = new NewCookie(TICKET_COOKIE_NAME, ticket.getId(), "/", domain, null, timeout * 3600, false);
      log.info("Successful Granting Ticket creation for user '{}' with CAS ID: {}", username, ticket.getId());
      return Response
          .created(UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(TicketResource.class).build(ticket.getId()))
          .header(HttpHeaders.SET_COOKIE, cookie).build();

    } catch(AuthenticationException e) {
      log.info("Authentication failure of user '{}' at ip: '{}': {}", username, servletRequest.getRemoteAddr(),
          e.getMessage());
      // When a request contains credentials and they are invalid, the a 403 (Forbidden) should be returned.
      throw new ForbiddenException();
    }
  }

  @GET
  @Path("/subject/{username}")
  public AuthDtos.SubjectDto get(@PathParam("username") String username, @QueryParam("application") String application,
      @QueryParam("key") String key) {
    validateApplication(application, key);
    User user = userService.findUser(username);
    AuthDtos.SubjectDto subject;
    if(user != null) {
      subject = dtos.asDto(user, true);
    } else {
      subject = AuthDtos.SubjectDto.newBuilder().setUsername(username).build();
    }
    return subject;
  }

  private Ticket createTicket(String username, boolean renew, boolean rememberMe, String application) {
    Ticket ticket;
    List<Ticket> tickets = ticketService.findByUsername(username);
    if(renew) ticketService.deleteAll(tickets);
    if(renew || tickets == null || tickets.isEmpty()) {
      ticket = new Ticket();
      ticket.setUsername(username);
    } else {
      ticket = tickets.get(0);
    }
    ticket.setRemembered(rememberMe);
    ticket.addLog(application, "login");
    ticketService.save(ticket);

    return ticket;
  }

}

