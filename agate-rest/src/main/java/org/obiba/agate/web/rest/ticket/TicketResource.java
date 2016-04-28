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
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.NoSuchUserException;
import org.obiba.agate.service.TicketService;
import org.obiba.agate.service.TokenUtils;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.rest.application.ApplicationAwareResource;
import org.obiba.shiro.realm.ObibaRealm;
import org.obiba.web.model.AuthDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 *
 */
@Component
@Path("/ticket")
@Scope("request")
public class TicketResource extends ApplicationAwareResource {

  private static final Logger log = LoggerFactory.getLogger(TicketResource.class);

  @Inject
  private TicketService ticketService;

  @Inject
  private UserService userService;

  @Inject
  private TokenUtils tokenUtils;

  @GET
  @Path("/{idOrToken}")
  @RequiresRoles("agate-administrator")
  public Agate.TicketDto getToken(@PathParam("idOrToken") String idOrToken) {
    return dtos.asDto(ticketService.getTicket(idOrToken));
  }

  /**
   * Get the user profile as a angular schema form model. See also
   * {@link org.obiba.agate.web.rest.config.ConfigurationResource#getProfileConfiguration()}. The user profile is
   * only accessible to authenticated applications to which the user has access.
   *
   * @return
   * @throws JSONException
   */
  @GET
  @Path("/{token}/profile")
  @Produces(APPLICATION_JSON)
  public Response getProfile(@Context HttpServletRequest servletRequest, @PathParam("token") String token,
    @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader) throws JSONException {
    validateApplication(authHeader);
    tokenUtils.validateAccessToken(token, getApplicationName());

    Ticket ticket = ticketService.getTicket(token);
    ticket.addEvent(getApplicationName(), "profile");
    ticketService.save(ticket);

    User user = userService.findActiveUser(ticket.getUsername());
    if(user == null) user = userService.findActiveUserByEmail(ticket.getUsername());
    if (user == null) throw NoSuchUserException.withName(ticket.getUsername());

    authorizationValidator.validateApplication(servletRequest, user, getApplicationName());

    return Response.ok(userService.getUserProfile(user).toString()).build();
  }

  /**
   * Update the user profile from a angular schema form model. See also
   * {@link org.obiba.agate.web.rest.config.ConfigurationResource#getProfileConfiguration()}.The user profile is
   * only accessible to authenticated applications to which the user has access.
   *
   * @return
   * @throws JSONException
   */
  @PUT
  @Path("/{token}/profile")
  @Consumes(APPLICATION_JSON)
  public Response updateProfile(@Context HttpServletRequest servletRequest, @PathParam("token") String token,
    @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader, String model) throws JSONException {
    validateApplication(authHeader);
    tokenUtils.validateAccessToken(token, getApplicationName());

    Ticket ticket = ticketService.getTicket(token);
    ticket.addEvent(getApplicationName(), "profile");
    ticketService.save(ticket);

    User user = userService.findActiveUser(ticket.getUsername());
    if(user == null) user = userService.findActiveUserByEmail(ticket.getUsername());
    if (user == null) throw NoSuchUserException.withName(ticket.getUsername());

    authorizationValidator.validateApplication(servletRequest, user, getApplicationName());

    userService.updateUserProfile(user, new JSONObject(model));

    return Response.ok().build();
  }

  @GET
  @Path("/{token}/_validate")
  public Response validate(@PathParam("token") String token,
      @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader) {
    validateApplication(authHeader);
    tokenUtils.validateAccessToken(token, getApplicationName());

    Ticket ticket = ticketService.getTicket(token);
    ticket.addEvent(getApplicationName(), "validate");
    ticketService.save(ticket);

    return Response.ok().build();
  }

  @GET
  @Path("/{token}/subject")
  public AuthDtos.SubjectDto get(@PathParam("token") String token,
    @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader) {
    validateApplication(authHeader);
    tokenUtils.validateAccessToken(token, getApplicationName());

    Ticket ticket = ticketService.getTicket(token);
    ticket.addEvent(getApplicationName(), "subject");
    ticketService.save(ticket);

    User user = userService.findActiveUser(ticket.getUsername());
    if(user == null) user = userService.findActiveUserByEmail(ticket.getUsername());
    AuthDtos.SubjectDto subject;

    if(user != null) {
      subject = dtos.asDto(user, true);
    } else {
      subject = AuthDtos.SubjectDto.newBuilder().setUsername(ticket.getUsername()).build();
    }

    return subject;
  }

  @GET
  @Path("/{token}/username")
  public Response getUsername(@Context HttpServletRequest servletRequest, @PathParam("token") String token,
    @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader) {
    validateApplication(authHeader);
    tokenUtils.validateAccessToken(token, getApplicationName());

    Ticket ticket = ticketService.getTicket(token);
    ticket.addEvent(getApplicationName(), "validate");
    ticketService.save(ticket);

    String username = ticket.getUsername();
    User user = userService.findActiveUser(username);
    if(user == null) user = userService.findActiveUserByEmail(username);
    authorizationValidator.validateApplication(servletRequest, user, getApplicationName());

    return Response.ok().header(HttpHeaders.SET_COOKIE, getCookie(ticket)).entity(ticket.getUsername()).build();
  }

  @DELETE
  @Path("/{idOrToken}")
  public Response logout(@PathParam("idOrToken") String idOrToken,
    @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader) {
    if(!SecurityUtils.getSubject().hasRole("agate-administrator")) {
      validateApplication(authHeader);
    }
    try {
      ticketService.delete(idOrToken);
    } catch(Exception e) {
      // ignore
    }

    Configuration configuration = getConfiguration();
    return Response.noContent().header(HttpHeaders.SET_COOKIE,
      new NewCookie(TicketsResource.TICKET_COOKIE_NAME, null, "/", configuration.getDomain(),
        "Obiba session deleted", 0, configuration.hasDomain())).build();
  }

  //
  // Private methods
  //

  private NewCookie getCookie(Ticket ticket) {
    String token = tokenUtils.makeAccessToken(ticket);
    Configuration configuration = getConfiguration();
    int timeout = ticket.isRemembered() ? configuration.getLongTimeout() : configuration.getShortTimeout();
    return new NewCookie(TicketsResource.TICKET_COOKIE_NAME, token, "/", configuration.getDomain(), null,
      timeout * 3600, configuration.hasDomain());
  }

}

