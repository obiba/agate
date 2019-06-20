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

import org.apache.shiro.subject.Subject;
import org.obiba.agate.domain.Group;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserStatus;
import org.obiba.agate.service.ApplicationService;
import org.obiba.agate.service.GroupService;
import org.obiba.shiro.authc.HttpAuthorizationToken;
import org.obiba.shiro.realm.ObibaRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.ws.rs.ForbiddenException;

@Component
public class AuthorizationValidator {

  private static final Logger log = LoggerFactory.getLogger(AuthorizationValidator.class);

  private final GroupService groupService;

  private final ApplicationService applicationService;

  @Inject
  public AuthorizationValidator(GroupService groupService, ApplicationService applicationService) {
    this.groupService = groupService;
    this.applicationService = applicationService;
  }

  public String validateApplication(String appAuthHeader) {
    if (appAuthHeader == null) throw new ForbiddenException();

    HttpAuthorizationToken token = new HttpAuthorizationToken(ObibaRealm.APPLICATION_AUTH_SCHEMA, appAuthHeader);
    String appName = token.getUsername();

    validateApplicationParameters(appName, new String(token.getPassword()));

    return appName;
  }

  public void validateRealm(ServletRequest servletRequest, User user, Subject subject) {
    // check that authentication realm is the expected one as specified in user profile
    if(!subject.getPrincipals().getRealmNames().contains(user.getRealm())) {
      log.info("Authentication failure of user '{}' at ip: '{}': unexpected realm '{}'", user.getName(),
        servletRequest.getRemoteAddr(), subject.getPrincipals().getRealmNames().iterator().next());
      throw new ForbiddenException();
    }
  }

  /**
     * Check user exists and has the right status.
     *
     * @param servletRequest
     * @param username
     * @param user
     */

  public void validateUser(ServletRequest servletRequest, String username, User user) {
    if(user == null) {
      log.warn("Not a registered user '{}' at ip: '{}'", username, servletRequest.getRemoteAddr());
      throw new ForbiddenException();
    } else if(user.getStatus() != UserStatus.ACTIVE) {
      log.warn("Not an active user '{}': status is '{}'", username, user.getStatus());
      throw new ForbiddenException();
    }
  }

  public void validateApplication(ServletRequest servletRequest, User user, String appName) {
    // check application
    if(!user.hasApplication(appName) && user.getGroups().stream().noneMatch(g -> {
      Group group = groupService.findGroup(g);
      return group != null && group.hasApplication(appName);
    })) {
      log.info("Application '{}' not allowed for user '{}' at ip: '{}'", appName, user.getName(),
        servletRequest.getRemoteAddr());
      throw new ForbiddenException();
    }
  }

  /**
   * Check application credentials and set current application name.
   *
   * @param name
   * @param key
   */
  public void validateApplicationParameters(String name, String key) {
    if(!applicationService.isValid(name, key)) throw new ForbiddenException();
  }

}
