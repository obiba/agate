/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
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
import java.util.Map;
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

import org.apache.commons.lang.LocaleUtils;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.MailService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.rest.application.ApplicationAwareResource;
import org.obiba.shiro.realm.ObibaRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.thymeleaf.spring4.SpringTemplateEngine;

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
  private SpringTemplateEngine templateEngine;

  @Inject
  private ConfigurationService configurationService;

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
   * @param body body of the message (optional)
   * @param template template name to be used if message body is not specified
   * @param authHeader
   * @return
   */
  @POST
  public Response notify(@Context HttpServletRequest servletRequest, @FormParam("username") List<String> usernames,
    @FormParam("group") List<String> groups, @FormParam("subject") String subject, @FormParam("body") String body,
    @FormParam("template") String template, @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader) {
    if(Strings.isNullOrEmpty(subject) && Strings.isNullOrEmpty(body)) return Response.noContent().build();

    validateApplication(authHeader);

    Set<User> recipients = Sets.newHashSet();
    if((usernames == null || usernames.isEmpty()) && (groups == null || groups.isEmpty())) {
      // all users having access to the application
      recipients.addAll(userService.findActiveUsersByApplication(getApplicationName()));
    } else {
      // all the specified users having access to the application
      appendRecipientsFromUsernames(usernames, recipients);
      // all the users belonging to one of the groups and having access to the application
      appendRecipientsFromGroup(groups, recipients);
    }

    if(Strings.isNullOrEmpty(template)) sendPlainEmail(subject, body, recipients);
    else sendTemplateEmail(subject, template, servletRequest.getParameterMap(), recipients);

    return Response.noContent().build();
  }

  //
  // Private methods
  //

  /**
   * Send an email by processing a template with request form parameters and the recipient
   * {@link org.obiba.agate.domain.User} as a context. The Template is expected to be located in a folder having
   * the application name.
   *
   * @param subject
   * @param templateName
   * @param context
   * @param recipients
   */
  private void sendTemplateEmail(String subject, String templateName, Map<String, String[]> context,
    Set<User> recipients) {
    org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
    context.forEach((k, v) -> {
      if(v != null && v.length == 1) {
        ctx.setVariable(k, v[0]);
      } else {
        ctx.setVariable(k, v);
      }
    });
    String templateLocation = getApplicationName() + "/" + templateName;

    recipients.forEach(rec -> {
      ctx.setVariable("user", rec);
      ctx.setLocale(LocaleUtils.toLocale(rec.getPreferredLanguage()));
      mailService
        .sendEmail(rec.getEmail(), subject, templateEngine.process(templateLocation, ctx));
    });
  }

  /**
   * Send an email build with
   * @param subject
   * @param body
   * @param recipients
   */
  private void sendPlainEmail(String subject, String body, Set<User> recipients) {
    recipients.forEach(rec -> mailService.sendEmail(rec.getEmail(), subject, body));
  }

  /**
   * Lookup active users and verify its access to the application requesting the notification.
   *
   * @param servletRequest
   * @param usernames
   * @param emails
   */
  private void appendRecipientsFromUsernames(Collection<String> usernames, Collection<User> recipients) {
    if(usernames == null || usernames.isEmpty()) return;

    List<String> applicationUsernames = userService.findActiveUsersByApplication(getApplicationName()).stream()
      .map(User::getName).collect(Collectors.toList());

    usernames.forEach(username -> {
      User user = userService.findActiveUser(username);
      if(user == null) user = userService.findActiveUserByEmail(username);

      if(user != null && applicationUsernames.contains(user.getName())) recipients.add(user);
    });
  }

  /**
   * Lookup active users belonging to one the groups and verify its access to the application requesting the notification.
   *
   * @param groups
   * @param emails
   */
  private void appendRecipientsFromGroup(Collection<String> groups, Collection<User> recipients) {
    if(groups == null || groups.isEmpty()) return;

    // find all users in each group and having access to the application
    groups.forEach(
      group -> userService.findActiveUsersByApplicationAndGroup(getApplicationName(), group).forEach(recipients::add));
  }
}
