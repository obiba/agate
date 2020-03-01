/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javax.ws.rs.core.Response.Status;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.bson.types.ObjectId;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.joda.time.DateTime;
import org.obiba.agate.domain.*;
import org.obiba.agate.security.AgateUserRealm;
import org.obiba.agate.security.Roles;
import org.obiba.agate.service.*;
import org.obiba.agate.web.rest.config.JerseyConfiguration;
import org.obiba.shiro.authc.HttpAuthorizationToken;
import org.obiba.shiro.realm.ObibaRealm;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Public resource for user join requests. Default realm is {@link AgateUserRealm}.
 */
@Component
@Path("/users")
public class UsersPublicResource {

  private static final String CURRENT_USER_NAME = "_current";

  private static final String[] BUILTIN_PARAMS =
    new String[] { "username", "firstname", "lastname", "application", "email", "locale", "group", "reCaptchaResponse", "realm" };

  private final UserService userService;

  private final ApplicationService applicationService;

  private final ConfigurationService configurationService;

  private final RealmConfigService realmConfigService;

  private final ReCaptchaService reCaptchaService;

  @Inject
  public UsersPublicResource(UserService userService,
                             ApplicationService applicationService,
                             ConfigurationService configurationService,
                             RealmConfigService realmConfigService,
                             ReCaptchaService reCaptchaService) {
    this.userService = userService;
    this.applicationService = applicationService;
    this.configurationService = configurationService;
    this.realmConfigService = realmConfigService;
    this.reCaptchaService = reCaptchaService;
  }

  @GET
  @Path("/i18n/{locale}.json")
  @Produces("application/json")
  public Response getUserProfileTranslations(@PathParam("locale") String locale) throws IOException {

    JsonNode userProfileTranslations = configurationService.getUserProfileTranslations(locale);

    return Response.ok(userProfileTranslations, "application/json").build();
  }

  @POST
  @Path("/_confirm")
  public Response confirm(@FormParam("key") String key, @FormParam("password") String password) {
    String username = configurationService.decrypt(key);
    User user = userService.findUser(username);

    if(user == null) throw new BadRequestException("User not found");

    if(user.getStatus() != UserStatus.APPROVED) throw new BadRequestException("Invalid user status.");

    userService.confirmUser(user, password);

    return Response.ok().build();
  }

  @POST
  @Path("/_forgot_password")
  public Response forgotPassword(@FormParam("username") String username) throws IOException {
    User user = userService.findUser(username);
    if(user == null) user = userService.findUserByEmail(username);
    UserCredentials userCredentials = user != null ? userService.findUserCredentials(user.getName()) : null;

    if(user != null && userCredentials != null) userService.resetPassword(user);

    if(user == null || userCredentials == null) throw new BadRequestException("User not found");

    return Response.ok().build();
  }

  @POST
  @Path("/_reset_password")
  public Response resetPassword(@FormParam("key") String key, @FormParam("password") String password)
    throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> data = mapper
      .readValue(configurationService.decrypt(key), new TypeReference<HashMap<String, String>>() {});

    if(DateTime.now().isAfter(DateTime.parse(data.get("expire")))) {
      throw new BadRequestException("Invalid key");
    }

    User user = userService.findUser(data.get("username"));

    if(user == null) throw new BadRequestException("User not found");

    UserCredentials userCredentials = userService.findUserCredentials(user.getName());

    if(userCredentials == null) throw new BadRequestException("user has no credentials defined");

    userCredentials.setPassword(userService.hashPassword(password));

    userService.save(userCredentials);

    return Response.noContent().build();
  }

  @POST
  @Path("/_test")
  public Response test(@RequestBody Map<String, String> values) {
    AuthenticationInfo authenticationInfo = userService.test(values.get("provider"), new UsernamePasswordToken(values.get("username"), values.get("password"))); // will throw AuthenticationException

    if (authenticationInfo == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    return Response.ok().build();
  }

  @POST
  @Path("/_join")
  public Response create(@FormParam("username") String username, @FormParam("firstname") String firstName,
    @FormParam("lastname") String lastName, @FormParam("email") String email, @FormParam("locale") String preferredLanguage,
    @FormParam("application") List<String> applications, @FormParam("group") List<String> groups,
    @FormParam("password") String password, @FormParam("realm") String realm, @FormParam("reCaptchaResponse") String reCaptchaResponse,
    @Context HttpServletRequest request) {
    if(Strings.isNullOrEmpty(email)) throw new BadRequestException("Email cannot be empty");

    if(!new EmailValidator().isValid(email, null)) throw new BadRequestException("Not a valid email address");

    String name = username;

    if(Strings.isNullOrEmpty(username)) {
      if(configurationService.getConfiguration().isJoinWithUsername())
        throw new BadRequestException("User name cannot be empty");

      try {
        name = email.split("@")[0];
      } catch(Exception e) {
        name = new ObjectId().toString();
      }
    }

    if(new EmailValidator().isValid(name, null)) throw new BadRequestException("User name cannot be an email address");

    if(!reCaptchaService.verify(reCaptchaResponse)) throw new BadRequestException("Invalid reCaptcha response");

    if(CURRENT_USER_NAME.equals(name)) throw new BadRequestException("Reserved user name: " + CURRENT_USER_NAME);

    String userRealm = Strings.isNullOrEmpty(realm) || AgateRealm.AGATE_USER_REALM.getName().equals(realm)
      ? AgateRealm.AGATE_USER_REALM.getName()
      : realmConfigService.getConfig(realm).getName();

    User user = userService.findUserByEmail(email);

    if(user != null) throw new BadRequestException("Email already in use: " + user.getEmail());

    user = userService.findUser(name);

    int i = 1;
    String originalName = name;
    while(user != null) {
      name = originalName + i;
      user = userService.findUser(name);
      i++;
    }

    user = User.newBuilder().name(name).realm(userRealm).role(Roles.AGATE_USER).pending()
      .firstName(firstName).lastName(lastName).email(email).preferredLanguage(preferredLanguage).build();
    user.setGroups(Sets.newHashSet(groups));
    user.setApplications(Sets.newHashSet(applications));
    user.setAttributes(extractAttributes(request));

    String applicationName = getRequestingApplication(request);
    if(!Strings.isNullOrEmpty(applicationName)) {
      user.getApplications().add(applicationName);
      if (!Strings.isNullOrEmpty(password))
        user.setStatus(UserStatus.ACTIVE);
      else
        user.setStatus(UserStatus.APPROVED);
    }

    userService.createUser(user, password);

    return Response
      .created(UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(UserResource.class).build(user.getId())).build();
  }

  private Map<String, String> extractAttributes(HttpServletRequest request) {
    final Map<String, AttributeConfiguration> attributes = configurationService.getConfiguration().getUserAttributes()
      .stream().collect(Collectors.toMap(a -> a.getName(), a -> a));
    final Map<String, String[]> params = request.getParameterMap();
    final Set<String> extraParams = Sets.difference(params.keySet(), Sets.newHashSet(Arrays.asList(BUILTIN_PARAMS)));

    Map<String, String> res = Maps.newHashMap();

    attributes.values().forEach(a -> {
      if(!params.containsKey(a.getName())) {
        if(a.isRequired()) throw new BadRequestException("Missing attribute " + a.getName());
      }
    });

    for(String param : extraParams) {
      String[] values = params.get(param);

      if(values.length > 1) {
        throw new BadRequestException("Invalid repeated parameter " + param);
      }

      if(attributes.containsKey(param)) {
        AttributeConfiguration attribute = attributes.get(param);
        res.put(attribute.getName(), getParsedAttribute(attribute, values[0]));
      } else {
        res.put(param, params.get(param)[0]);
      }
    }

    res.remove("password");
    return res;
  }

  private String getParsedAttribute(AttributeConfiguration attribute, String value) {
    String parsedValue;

    try {
      switch(attribute.getType()) {
        case INTEGER:
          parsedValue = Integer.valueOf(value).toString();
          break;
        case BOOLEAN:
          parsedValue = Boolean.valueOf(value).toString();
          break;
        case NUMBER:
          parsedValue = Float.valueOf(value).toString();
          break;
        default:
          parsedValue = value;
      }
    } catch(NumberFormatException e) {
      throw new BadRequestException("Invalid value " + value);
    }

    if(attribute.hasValues() && !attribute.getValues().contains(parsedValue)) {
      throw new BadRequestException("Invalid value " + parsedValue);
    }

    return parsedValue;
  }

  protected String getRequestingApplication(HttpServletRequest servletRequest) {
    String appAuthHeader = servletRequest.getHeader(ObibaRealm.APPLICATION_AUTH_HEADER);
    if(appAuthHeader == null) return null;

    HttpAuthorizationToken token = new HttpAuthorizationToken(ObibaRealm.APPLICATION_AUTH_SCHEMA, appAuthHeader);
    return applicationService.isValid(token.getUsername(), new String(token.getPassword())) ? token.getUsername() : null;
  }
}
