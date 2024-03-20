/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.ticket;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.TicketService;
import org.obiba.agate.service.TokenUtils;
import org.obiba.agate.service.TotpService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.rest.application.ApplicationAwareResource;
import org.obiba.agate.web.rest.config.JerseyConfiguration;
import org.obiba.agate.web.rest.security.ClientIPUtils;
import org.obiba.shiro.NoSuchOtpException;
import org.obiba.shiro.authc.UsernamePasswordOtpToken;
import org.obiba.shiro.realm.ObibaRealm;
import org.obiba.web.model.AuthDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;

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

  @Inject
  private TokenUtils tokenUtils;

  @Inject
  private TotpService totpService;

  @GET
  @RequiresRoles("agate-administrator")
  public List<Agate.TicketDto> get() {
    ImmutableList.Builder<Agate.TicketDto> builder = ImmutableList.builder();
    for (Ticket ticket : ticketService.findAll()) {
      builder.add(dtos.asDto(ticket));
    }
    return builder.build();
  }

  @POST
  public Response login(@Context HttpServletRequest servletRequest,
                        @QueryParam("rememberMe") @DefaultValue("false") boolean rememberMe,
                        @QueryParam("renew") @DefaultValue("true") boolean renew, @FormParam("username") String username,
                        @FormParam("password") String password, @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader) {
    validateApplication(authHeader);

    Subject subject = null;
    try {
      User user = userService.findActiveUser(username);

      if (user == null) user = userService.findActiveUserByEmail(username);

      authorizationValidator.validateUser(servletRequest, username, user);
      authorizationValidator.validateApplication(servletRequest, user, getApplicationName());

      // check authentication
      subject = SecurityUtils.getSubject();
      assert user != null;

      try {
        subject.login(makeUsernamePasswordToken(user.getName(), password, servletRequest));
        authorizationValidator.validateRealm(servletRequest, user, subject);
      } catch (NoSuchOtpException e) {
        JSONObject otp = null;
        if (getConfiguration().isEnforced2FA() && !user.hasSecret()) {
          if (getConfiguration().isEnforced2FAWithEmail()) {
            userService.applyAndSendOtp(user);
          } else {
            otp = userService.applyTempSecret(user);
          }
        }
        Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST).header("WWW-Authenticate", e.getOtpHeader());
        if (otp != null) {
          builder.entity(otp.toString()).header("Content-type", "application/json");
        }
        return builder.build();
      }

      if (log.isDebugEnabled())
        log.debug("User '{}' has {} ticket(s)", user.getName(), ticketService.findByUsername(user.getName()).size());
      Ticket ticket = ticketService.create(user.getName(), renew, rememberMe, getApplicationName());
      String token = tokenUtils.makeAccessToken(ticket);
      Configuration configuration = getConfiguration();
      int timeout = rememberMe ? configuration.getLongTimeout() : configuration.getShortTimeout();
      NewCookie cookie = new NewCookie(TICKET_COOKIE_NAME, token, "/", configuration.getDomain(), null,
          timeout * 3600, true, true);

      user = userService.getUser(user.getId()); // refresh after login
      user.setLastLogin(DateTime.now());
      userService.save(user);

      if (log.isDebugEnabled())
        log.info("Successful login for user '{}' from application '{}' with token: {}", username, getApplicationName(), token);
      else
        log.info("Successful login for user '{}' from application '{}'", username, getApplicationName());
      return Response
          .created(UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(TicketResource.class).build(token))
          .header(HttpHeaders.SET_COOKIE, cookie).build();

    } catch (AuthenticationException e) {
      log.info("Authentication failure of user '{}' at ip: '{}': {}", username, ClientIPUtils.getClientIP(servletRequest),
          e.getMessage());
      // When a request contains credentials and they are invalid, the a 403 (Forbidden) should be returned.
      throw new ForbiddenException();
    } finally {
      if (subject != null) subject.logout();
    }
  }

  @GET
  @Path("/subject/{username}")
  public AuthDtos.SubjectDto get(@PathParam("username") String username, @QueryParam("application") String application,
                                 @QueryParam("key") String key, @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader) {
    validateApplication(authHeader);

    User user = userService.findActiveUser(username);
    if (user == null) user = userService.findActiveUserByEmail(username);
    AuthDtos.SubjectDto subject;
    if (user != null) {
      subject = dtos.asDto(user, true);
    } else {
      subject = AuthDtos.SubjectDto.newBuilder().setUsername(username).build();
    }
    return subject;
  }

  private UsernamePasswordToken makeUsernamePasswordToken(String username, String password, HttpServletRequest request) {
    String otp = request.getHeader("X-Obiba-TOTP");
    if (!Strings.isNullOrEmpty(otp))
      return new UsernamePasswordOtpToken(username, password, otp);
    return new UsernamePasswordToken(username, password);
  }

}
