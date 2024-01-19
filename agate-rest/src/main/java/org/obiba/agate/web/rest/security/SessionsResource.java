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

import com.google.common.base.Strings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.rest.config.JerseyConfiguration;
import org.obiba.shiro.NoSuchOtpException;
import org.obiba.shiro.authc.UsernamePasswordOtpToken;
import org.obiba.shiro.web.filter.AuthenticationExecutor;
import org.obiba.shiro.web.filter.UserBannedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static jakarta.ws.rs.core.Cookie.DEFAULT_VERSION;
import static jakarta.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;

@Component
@Path("/auth")
public class SessionsResource {

  private static final Logger log = LoggerFactory.getLogger(SessionsResource.class);

  @Inject
  private AuthenticationExecutor authenticationExecutor;

  @Inject
  private UserService userService;

  @Inject
  private ConfigurationService configurationService;

  @POST
  @Path("/sessions")
  public Response createSession(@SuppressWarnings("TypeMayBeWeakened") @Context HttpServletRequest servletRequest,
      @FormParam("username") String username, @FormParam("password") String password) {
    try {
      authenticationExecutor.login(makeUsernamePasswordToken(username, password, servletRequest));
      String sessionId = SecurityUtils.getSubject().getSession().getId().toString();
      userService.updateUserLastLogin(username);
      log.info("Successful session creation for user '{}' session ID is '{}'.", username, sessionId);

      Response.ResponseBuilder builder = Response.created(
        UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(SessionResource.class).build(sessionId));

      User user = userService.findUser(username);
      if (user != null && !Strings.isNullOrEmpty(user.getPreferredLanguage())) {
        builder.cookie(new NewCookie("NG_TRANSLATE_LANG_KEY", user.getPreferredLanguage(), configurationService.getContextPath() + "/", null, DEFAULT_VERSION, null, DEFAULT_MAX_AGE, null, false, false));
      }

      return builder.build();
    } catch(UserBannedException e) {
      throw e;
    } catch (NoSuchOtpException e) {
      return Response.status(Response.Status.UNAUTHORIZED).header("WWW-Authenticate", e.getOtpHeader()).build();
    } catch(AuthenticationException e) {
      log.info("Authentication failure of user '{}' at ip: '{}': {}", username, ClientIPUtils.getClientIP(servletRequest),
          e.getMessage());
      // When a request contains credentials and they are invalid, the a 403 (Forbidden) should be returned.
      return Response.status(Response.Status.FORBIDDEN).cookie().build();
    }
  }

  private UsernamePasswordToken makeUsernamePasswordToken(String username, String password, HttpServletRequest request) {
    String otp = request.getHeader("X-Obiba-TOTP");
    if (!Strings.isNullOrEmpty(otp))
      return new UsernamePasswordOtpToken(username, password, otp);
    return new UsernamePasswordToken(username, password);
  }
}

