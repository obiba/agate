/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.notification;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.obiba.agate.domain.User;
import org.obiba.agate.service.MailService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.rest.application.ApplicationAwareResource;
import org.obiba.shiro.realm.ObibaRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

@Component
@Path("/notifications")
@Scope("request")
public class NotificationsResource extends ApplicationAwareResource {

  private static final Logger log = LoggerFactory.getLogger(NotificationsResource.class);

  @Inject
  private MailService mailService;

  @Inject
  private UserService userService;

  /**
   * Send a notification email to all active users matching the provided user names and/or belonging to the provided groups AND
   * having access to the requesting application. If no user names and no groups are specified, all active users having
   * access to the application will be notified.
   *
   * @param servletRequest
   * @param usernames
   * @param groups
   * @param subject
   * @param body
   * @param authHeader
   * @return
   */
  @POST
  public Response notify(@Context HttpServletRequest servletRequest, @FormParam("username") List<String> usernames,
    @FormParam("group") List<String> groups, @FormParam("subject") String subject, @FormParam("body") String body,
    @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader) {
    if(Strings.isNullOrEmpty(subject) && Strings.isNullOrEmpty(body)) return Response.noContent().build();

    validateApplication(authHeader);

    Set<String> emails = Sets.newHashSet();
    if((usernames == null || usernames.isEmpty()) && (groups == null || groups.isEmpty())) {
      // all users having access to the application
      userService.findActiveUsersByApplication(getApplicationName()).forEach(user -> emails.add(user.getEmail()));
    } else {
      // all the specified users having access to the application
      appendEmailsFromUsernames(usernames, emails);
      // all the users belonging to one of the groups and having access to the application
      appendEmailsFromGroup(groups, emails);
    }

    emails.forEach(email -> mailService.sendEmail(email, subject, body));

    return Response.noContent().build();
  }

  //
  // Private methods
  //

  /**
   * Lookup active users and verify its access to the application requesting the notification.
   *
   * @param servletRequest
   * @param usernames
   * @param emails
   */
  private void appendEmailsFromUsernames(Collection<String> usernames, Collection<String> emails) {
    if(usernames == null || usernames.isEmpty()) return;

    List<String> applicationUsernames = userService.findActiveUsersByApplication(getApplicationName()).stream()
      .map(User::getName).collect(Collectors.toList());

    usernames.forEach(username -> {
      User user = userService.findActiveUser(username);
      if(user == null) user = userService.findActiveUserByEmail(username);

      if(user != null && applicationUsernames.contains(user.getName())) emails.add(user.getEmail());
    });
  }

  /**
   * Lookup active users belonging to one the groups and verify its access to the application requesting the notification.
   *
   * @param groups
   * @param emails
   */
  private void appendEmailsFromGroup(Collection<String> groups, Collection<String> emails) {
    if(groups == null || groups.isEmpty()) {
      // find all users from any group and having access to the application
      userService.findActiveUsersByApplication(getApplicationName()).forEach(user -> emails.add(user.getEmail()));
    } else {
      // find all users in each group and having access to the application
      groups.forEach(group -> userService.findActiveUsersByApplicationAndGroup(getApplicationName(), group)
        .forEach(user -> emails.add(user.getEmail())));
    }
  }
}
