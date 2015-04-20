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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.TicketService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.model.Agate;
import org.obiba.web.model.AuthDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@Path("/ticket/{token}")
public class TicketResource extends BaseTicketResource {

  private static final Logger log = LoggerFactory.getLogger(TicketResource.class);

  @Inject
  private TicketService ticketService;

  @Inject
  private UserService userService;

  @GET
  @RequiresRoles("agate-administrator")
  public Agate.TicketDto getToken(@PathParam("token") String token) {
    return dtos.asDto(ticketService.getTicket(token));
  }

  @GET
  @Path("/subject")
  public AuthDtos.SubjectDto get(@PathParam("token") String token, @Context HttpServletRequest servletRequest) {
    Ticket ticket = ticketService.getTicket(token);
    ticket.addEvent(getApplicationName(), "subject");
    ticketService.save(ticket);

    User user = userService.findUser(ticket.getUsername());
    AuthDtos.SubjectDto subject;

    if(user != null) {
      subject = dtos.asDto(user, true);
    } else {
      subject = AuthDtos.SubjectDto.newBuilder().setUsername(ticket.getUsername()).build();
    }

    return subject;
  }

  @GET
  @Path("/username")
  public Response getUsername(@PathParam("token") String token, @Context HttpServletRequest servletRequest) {
    validateApplication(servletRequest);

    Ticket ticket = ticketService.getTicket(token);
    ticket.addEvent(getApplicationName(), "validate");
    ticketService.save(ticket);

    return Response.ok().entity(ticket.getUsername()).build();
  }

  @DELETE
  public Response logout(@PathParam("token") String token, @Context HttpServletRequest servletRequest) {
    if(SecurityUtils.getSubject().hasRole("agate-administrator")) {
      ticketService.delete(token);

      return Response.ok().build();
    }

    validateApplication(servletRequest);
    ticketService.delete(token);

    return Response.noContent().header(HttpHeaders.SET_COOKIE,
        new NewCookie(TicketsResource.TICKET_COOKIE_NAME, null, "/", getConfiguration().getDomain(),
            "Obiba session deleted", 0, false)).build();
  }

}

