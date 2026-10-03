/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.config;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.google.common.base.Strings;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.obiba.agate.domain.Application;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserProfile;
import org.obiba.agate.service.ApplicationService;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.NoSuchUserException;
import org.obiba.agate.service.NotificationTemplateService;
import org.obiba.agate.service.NotificationTemplateService.NotificationTemplate;
import org.obiba.agate.service.UserService;
import org.obiba.agate.service.support.MessageResolverMethod;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Notification templates of the Agate home ({@code AGATE_HOME/conf/templates/notifications/}): Agate's own ones
 * (no folder) and the application ones ({@code folder} is the application name).
 */
@Component
@Path("/config/notification-templates")
@RequiresRoles("agate-administrator")
public class NotificationTemplatesResource {

  // locale suffix of a template name, e.g. confirmationEmail_fr or confirmationEmail_fr_CA
  private static final Pattern LOCALE_SUFFIX = Pattern.compile("_([a-z]{2}(_[A-Z]{2})?)$");

  private final NotificationTemplateService notificationTemplateService;

  private final UserService userService;

  private final ConfigurationService configurationService;

  private final MessageSource messageSource;

  private final ApplicationService applicationService;

  @Inject
  public NotificationTemplatesResource(NotificationTemplateService notificationTemplateService, UserService userService,
                                       ConfigurationService configurationService, MessageSource messageSource,
                                       ApplicationService applicationService) {
    this.notificationTemplateService = notificationTemplateService;
    this.userService = userService;
    this.configurationService = configurationService;
    this.messageSource = messageSource;
    this.applicationService = applicationService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response list(@QueryParam("folder") String folder) {
    return handle(() -> {
      List<NotificationTemplate> templates = notificationTemplateService.list(folder, fallbackOf(folder));
      return Response.ok(templates).build();
    });
  }

  @GET
  @Path("/{name}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response get(@QueryParam("folder") String folder, @PathParam("name") String name) {
    return handle(() -> Response.ok(notificationTemplateService.read(folder, name, fallbackOf(folder))).build());
  }

  @PUT
  @Path("/{name}")
  @Consumes(MediaType.TEXT_PLAIN)
  public Response save(@QueryParam("folder") String folder, @PathParam("name") String name, String content) {
    return handle(() -> {
      notificationTemplateService.write(folder, name, content);
      return Response.ok().build();
    });
  }

  /**
   * Render a (not necessarily saved) template content, with the current user as the recipient and sample values.
   *
   * @param locale language of the rendering, defaults to the template name suffix (e.g. _fr), else English
   */
  @POST
  @Path("/{name}/_preview")
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_HTML)
  public Response preview(@QueryParam("folder") String folder, @PathParam("name") String name,
                          @QueryParam("locale") String localeParam, String content) {
    return handle(() -> {
      Matcher matcher = LOCALE_SUFFIX.matcher(name);
      Locale locale = !Strings.isNullOrEmpty(localeParam) ? LocaleUtils.toLocale(localeParam)
        : matcher.find() ? LocaleUtils.toLocale(matcher.group(1)) : Locale.ENGLISH;
      User user = currentOrSampleUser();
      Map<String, Object> model = new HashMap<>();
      model.put("msg", new MessageResolverMethod(messageSource, locale));
      if (Strings.isNullOrEmpty(folder)) {
        // see UserService emails
        model.put("user", new UserProfile(user));
        model.put("organization", configurationService.getConfiguration().getName());
        model.put("publicUrl", configurationService.getPublicUrl());
        model.put("key", "preview-key");
        model.put("code", "123456");
        model.put("timeout", 5);
      } else {
        // see NotificationsResource, other variables are provided by the application
        model.put("user", user);
      }
      return Response.ok(notificationTemplateService.preview(name, content, model)).build();
    });
  }

  @DELETE
  @Path("/{name}")
  public Response delete(@QueryParam("folder") String folder, @PathParam("name") String name) {
    return handle(() -> {
      notificationTemplateService.delete(folder, name);
      return Response.noContent().build();
    });
  }

  /**
   * Fallback templates folder of the application which folder is this one, see NotificationsResource.
   */
  private String fallbackOf(String folder) {
    if (Strings.isNullOrEmpty(folder)) return null;
    Application application = applicationService.findByIdOrName(folder);
    return application == null ? null : application.getNotificationsTemplate();
  }

  private User currentOrSampleUser() {
    try {
      return userService.getCurrentUser();
    } catch (NoSuchUserException e) {
      // administrator not stored in Agate (e.g. from shiro.ini)
      return User.newBuilder("jdoe").firstName("John").lastName("Doe").email("jdoe@example.org").build();
    }
  }

  private static Response handle(Callable<Response> action) {
    try {
      return action.call();
    } catch (IllegalArgumentException e) {
      return error(Response.Status.BAD_REQUEST, e.getMessage());
    } catch (NoSuchElementException e) {
      return error(Response.Status.NOT_FOUND, e.getMessage());
    } catch (IOException e) {
      return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Response error(Response.Status status, String message) {
    return Response.status(status).type(MediaType.APPLICATION_JSON)
      .entity(Map.of("code", status.getStatusCode(), "status", status.name(), "message", String.valueOf(message)))
      .build();
  }
}
