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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.agate.domain.Ticket;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.TicketService;
import org.obiba.agate.service.UserService;
import org.obiba.web.model.AuthDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@Path("/ticket/{ticket}")
public class TicketResource extends BaseTicketResource {

  private static final Logger log = LoggerFactory.getLogger(TicketResource.class);

  @PathParam("ticket")
  private String ticket;

  @Inject
  private TicketService ticketService;

  @Inject
  private UserService userService;


  @GET
  @Path("/subject")
  public AuthDtos.SubjectDto get(@QueryParam("application") String application, @QueryParam("key") String key) {
    validateApplication(application, key);

    Ticket ticket = ticketService.findById(this.ticket);
    ticket.addLog(application, "subject");
    ticketService.save(ticket);
    AuthDtos.SubjectDto.Builder builder = AuthDtos.SubjectDto.newBuilder().setUsername(ticket.getUsername());
    User user = userService.findUser(ticket.getUsername());
    if(user != null) {
      builder.addAllGroups(user.getGroups());
    }
    return builder.build();
  }

  @GET
  @Path("/username")
  public Response getUsername(@QueryParam("application") String application, @QueryParam("key") String key) {
    validateApplication(application, key);

    Ticket ticket = ticketService.findById(this.ticket);
    ticket.addLog(application, "validate");
    ticketService.save(ticket);
    return Response.ok().entity(ticket.getUsername()).build();
  }

  @DELETE
  public Response logout() {
    ticketService.delete(ticket);
    return Response.noContent().build();
  }

}

