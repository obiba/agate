/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.user;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTime;
import org.obiba.agate.domain.AttributeConfiguration;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserCredentials;
import org.obiba.agate.domain.UserStatus;
import org.obiba.agate.security.AgateUserRealm;
import org.obiba.agate.security.Roles;
import org.obiba.agate.service.ApplicationService;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.rest.config.JerseyConfiguration;
import org.obiba.shiro.authc.HttpAuthorizationToken;
import org.obiba.shiro.realm.ObibaRealm;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Public resource for user join requests. Default realm is {@link org.obiba.agate.security.AgateUserRealm}.
 */
@Component
@Path("/users")
public class UsersPublicResource {

  private static final String CURRENT_USER_NAME = "_current";

  private static final String[] REQUIRED_PARAMS = new String[] { "username", "firstname", "lastname", "application", //
    "email", "groups" };

  @Inject
  private UserService userService;

  @Inject
  private ApplicationService applicationService;

  @Inject
  private ConfigurationService configurationService;

  @POST
  @Path("/_confirm")
  public Response confirm(@FormParam("key")String key, @FormParam("password")String password) {
    String username = configurationService.decrypt(key);
    User user = userService.findUser(username);

    if (user == null)
      throw new BadRequestException("User not found");

    if (user.getStatus() != UserStatus.APPROVED)
      throw new BadRequestException("Invalid user status.");

    userService.confirmUser(user, password);

    return Response.ok().build();
  }

  @POST
  @Path("/_reset_password")
  public Response resetPassword(@FormParam("key")String key, @FormParam("password")String password)
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

    if(userCredentials == null) new BadRequestException("user has no credentials defined");

    userCredentials.setPassword(userService.hashPassword(password));

    userService.save(userCredentials);

    return Response.noContent().build();
  }

  @POST
  @Path("/_join")
  public Response create(@FormParam("username") String username, @FormParam("firstname") String firstName,
    @FormParam("lastname") String lastName, @FormParam("email") String email,
    @FormParam("application") List<String> applications, @FormParam("group") List<String> groups,
    @Context HttpServletRequest request) {
    if(Strings.isNullOrEmpty(email)) throw new BadRequestException("Email cannot be empty");

    String name = Strings.isNullOrEmpty(username) ? email : username;

    if(CURRENT_USER_NAME.equals(name)) throw new BadRequestException("Reserved user name: " + CURRENT_USER_NAME);

    User user = userService.findUser(name);

    if(user != null) {
      throw new BadRequestException("User already exists: " + name);
    }

    user = User.newBuilder().name(name).realm(AgateUserRealm.AGATE_REALM).role(Roles.AGATE_USER)
      .pending().firstName(firstName).lastName(lastName).email(email).build();
    user.setGroups(Sets.newHashSet(groups));
    user.setApplications(Sets.newHashSet(applications));
    user.setAttributes(extractAttributes(request));

    if (isRequestedByApplication(request)) {
      user.setStatus(UserStatus.ACTIVE);
    }

    userService.createUser(user);

    return Response
      .created(UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(UserResource.class).build(user.getId())).build();
  }

  private Map<String, String> extractAttributes(HttpServletRequest request) {
    final Map<String, AttributeConfiguration> attributes = configurationService.getConfiguration().getUserAttributes()
      .stream().collect(Collectors.toMap(a -> a.getName(), a -> a));
    final Map<String, String[]> params = request.getParameterMap();
    final Set<String> extraParams = Sets.difference(params.keySet(), Sets.newHashSet(Arrays.asList(REQUIRED_PARAMS)));

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
        case DECIMAL:
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

  protected boolean isRequestedByApplication(HttpServletRequest servletRequest) {
    String appAuthHeader = servletRequest.getHeader(ObibaRealm.APPLICATION_AUTH_HEADER);
    if (appAuthHeader == null) return false;

    HttpAuthorizationToken token = new HttpAuthorizationToken(ObibaRealm.APPLICATION_AUTH_SCHEMA, appAuthHeader);
    return applicationService.isValid(token.getUsername(), new String(token.getPassword()));
  }
}
