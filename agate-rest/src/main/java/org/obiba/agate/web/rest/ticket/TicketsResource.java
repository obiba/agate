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
import javax.ws.rs.HeaderParam;
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
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.joda.time.DateTime;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.TicketService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.rest.application.ApplicationAwareResource;
import org.obiba.agate.web.rest.config.JerseyConfiguration;
import org.obiba.shiro.realm.ObibaRealm;
import org.obiba.web.model.AuthDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

/**
 *
 */
@Component
@Path("/tickets")
@Scope("request")
public class TicketsResource extends ApplicationAwareResource {

  private static final Logger log = LoggerFactory.getLogger(TicketsResource.class);

  public static final String TICKET_COOKIE_NAME = "obibaid";

  @Inject
  private TicketService ticketService;

  @Inject
  private UserService userService;

  @GET
  @RequiresRoles("agate-administrator")
  public List<Agate.TicketDto> get() {
    ImmutableList.Builder<Agate.TicketDto> builder = ImmutableList.builder();
    for(Ticket ticket : ticketService.findAll()) {
      builder.add(dtos.asDto(ticket));
    }
    return builder.build();
  }

  @POST
  public Response login(@Context HttpServletRequest servletRequest,
    @QueryParam("rememberMe") @DefaultValue("false") boolean rememberMe,
    @QueryParam("renew") @DefaultValue("false") boolean renew, @FormParam("username") String username,
    @FormParam("password") String password, @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader) {
    validateApplication(authHeader);

    Subject subject = null;
    try {
      User user = userService.findActiveUser(username);

      if(user == null) user = userService.findActiveUserByEmail(username);

      authorizationValidator.validateUser(servletRequest, username, user);
      authorizationValidator.validateApplication(servletRequest, user, getApplicationName());

      // check authentication
      subject = SecurityUtils.getSubject();
      assert user != null;
      subject.login(new UsernamePasswordToken(user.getName(), password));
      authorizationValidator.validateRealm(servletRequest, user, subject);

      Ticket ticket = ticketService.createTicket(user.getName(), renew, rememberMe, getApplicationName());
      Configuration configuration = getConfiguration();
      int timeout = rememberMe ? configuration.getLongTimeout() : configuration.getShortTimeout();
      NewCookie cookie = new NewCookie(TICKET_COOKIE_NAME, ticket.getToken(), "/", configuration.getDomain(), null,
        timeout * 3600, false);

      user.setLastLogin(DateTime.now());
      userService.save(user);

      log.info("Successful Granting Ticket creation for user '{}' with ticket ID: {}", username, ticket.getToken());
      return Response
        .created(UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(TicketResource.class).build(ticket.getToken()))
        .header(HttpHeaders.SET_COOKIE, cookie).build();

    } catch(AuthenticationException e) {
      log.info("Authentication failure of user '{}' at ip: '{}': {}", username, servletRequest.getRemoteAddr(),
        e.getMessage());
      // When a request contains credentials and they are invalid, the a 403 (Forbidden) should be returned.
      throw new ForbiddenException();
    } finally {
      if(subject != null) subject.logout();
    }
  }

  @GET
  @Path("/subject/{username}")
  public AuthDtos.SubjectDto get(@PathParam("username") String username, @QueryParam("application") String application,
    @QueryParam("key") String key, @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader) {
    validateApplication(authHeader);

    User user = userService.findActiveUser(username);
    AuthDtos.SubjectDto subject;
    if(user != null) {
      subject = dtos.asDto(user, true);
    } else {
      subject = AuthDtos.SubjectDto.newBuilder().setUsername(username).build();
    }
    return subject;
  }



}
