/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.notification;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang.LocaleUtils;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.MailService;
import org.obiba.agate.service.ReCaptchaService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.service.support.MessageResolverMethod;
import org.obiba.agate.web.rest.application.ApplicationAwareResource;
import org.obiba.shiro.realm.ObibaRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Path("/notifications")
@Scope("request")
public class NotificationsResource extends ApplicationAwareResource {

  private static final Logger log = LoggerFactory.getLogger(NotificationsResource.class);

  @Inject
  private MailService mailService;

  @Inject
  private Configuration freemarkerConfiguration;

  @Inject
  private MessageSource messageSource;

  @Inject
  private UserService userService;

  @Inject
  private ReCaptchaService reCaptchaService;

  /**
   * Send a notification email to all active users matching the provided user names and/or belonging to the provided groups AND
   * having access to the requesting application. If no user names and no groups are specified, all active users having
   * access to the application will be notified.
   *
   * @param formParams
   * @param authHeader
   * @return
   */
  @POST
  public Response notify(MultivaluedMap<String, String> formParams, @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader) {
    List<String> usernames = formParams.get("username");
    List<String> groups = formParams.get("group");
    String subject = formParams.getFirst("subject");
    // body of the message if the message template is not specified
    String body = formParams.getFirst("body");
    // template name to be used
    String template = formParams.getFirst("template");
    if (Strings.isNullOrEmpty(subject) && Strings.isNullOrEmpty(body)) return Response.noContent().build();

    validateApplication(authHeader);

    Set<User> recipients = Sets.newHashSet();
    if ((usernames == null || usernames.isEmpty()) && (groups == null || groups.isEmpty())) {
      // all users having access to the application
      recipients.addAll(userService.findActiveUsersByApplication(getApplicationName()));
    } else {
      // all the specified users having access to the application
      appendRecipientsFromUsernames(usernames, recipients);
      // all the users belonging to one of the groups and having access to the application
      appendRecipientsFromGroup(groups, recipients);
    }

    if (Strings.isNullOrEmpty(template)) sendPlainEmail(subject, body, recipients);
    else {
      List<String> reservedKeys = Lists.newArrayList("username", "group", "subject", "body", "template");
      Map<String, String[]> context = Maps.newHashMap();
      formParams.entrySet().stream()
          .filter(entry -> !reservedKeys.contains(entry.getKey()))
          . forEach(entry -> context.put(entry.getKey(), entry.getValue().toArray(new String[0])));
      sendTemplateEmail(subject, template, context, recipients);
    }

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

    String templateLocation = "notifications/" + getApplicationName() + "/" + templateName + ".ftl";

    Map<String, Object> ctx = Maps.newHashMap();
    context.forEach((k, v) -> {
      if (v != null && v.length == 1) {
        ctx.put(k, v[0]);
      } else {
        ctx.put(k, v);
      }
    });

    if (ctx.containsKey("reCaptcha")) {
      reCaptchaService.verify(ctx.get("reCaptcha").toString());
    }

    for (User rec : recipients) {
      Locale locale = LocaleUtils.toLocale(rec.getPreferredLanguage());
      ctx.put("user", rec);
      ctx.put("msg", new MessageResolverMethod(messageSource, locale));
      try {
        Template template = freemarkerConfiguration.getTemplate(templateLocation, locale);
        mailService.sendEmail(rec.getEmail(), subject,
          FreeMarkerTemplateUtils.processTemplateIntoString(template, ctx));
      } catch (Exception e) {
        log.error("Error while handling template {}", templateLocation, e);
      }
    }
  }

  /**
   * Send an email build with
   *
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
   * @param usernames
   * @param recipients
   */
  private void appendRecipientsFromUsernames(Collection<String> usernames, Collection<User> recipients) {
    if (usernames == null || usernames.isEmpty()) return;

    List<String> applicationUsernames = userService.findActiveUsersByApplication(getApplicationName()).stream()
      .map(User::getName).collect(Collectors.toList());

    usernames.forEach(username -> {
      User user = userService.findActiveUser(username);
      if (user == null) user = userService.findActiveUserByEmail(username);

      if (user != null && applicationUsernames.contains(user.getName())) recipients.add(user);
    });
  }

  /**
   * Lookup active users belonging to one the groups and verify its access to the application requesting the notification.
   *
   * @param groups
   * @param recipients
   */
  private void appendRecipientsFromGroup(Collection<String> groups, Collection<User> recipients) {
    if (groups == null || groups.isEmpty()) return;

    // find all users in each group and having access to the application
    groups.forEach(
      group -> userService.findActiveUsersByApplicationAndGroup(getApplicationName(), group).forEach(recipients::add));
  }
}
