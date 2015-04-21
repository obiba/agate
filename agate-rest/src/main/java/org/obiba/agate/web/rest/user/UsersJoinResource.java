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

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.obiba.agate.domain.AttributeConfiguration;
import org.obiba.agate.domain.User;
import org.obiba.agate.security.AgateUserRealm;
import org.obiba.agate.security.Roles;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.rest.config.JerseyConfiguration;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Public resource for user join requests. Default realm is {@link org.obiba.agate.security.AgateUserRealm}.
 */
@Component
@Path("/users/_join")
public class UsersJoinResource {

  private static final String CURRENT_USER_NAME = "_current";

  @Inject
  private UserService userService;

  @Inject
  private ConfigurationService configurationService;

  @POST
  public Response create(@FormParam("username") String username, @FormParam("firstname") String firstName,
    @FormParam("lastname") String lastName, @FormParam("email") String email,
    @FormParam("application") String application, @FormParam("groups") List<String> groups,
    @Context HttpServletRequest request) {
    if(Strings.isNullOrEmpty(username)) throw new BadRequestException("User name cannot be empty");

    if(userService.findUser(username) != null) throw new BadRequestException("User already exists: " + username);

    if(CURRENT_USER_NAME.equals(username)) throw new BadRequestException("Reserved user name: " + CURRENT_USER_NAME);

    final User user = User.newBuilder().name(username).realm(AgateUserRealm.AGATE_REALM).role(Roles.AGATE_USER)
      .pending().firstName(firstName).lastName(lastName).email(email).build();
    user.setGroups(Sets.newHashSet(groups));
    user.setApplications(Sets.newHashSet(application));
    user.setAttributes(extractAttributes(request));

    userService.save(user);

    return Response
      .created(UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(UserResource.class).build(user.getId())).build();
  }

  private Map<String, String> extractAttributes(HttpServletRequest request) {
    final List<AttributeConfiguration> attributes = configurationService.getConfiguration().getUserAttributes();
    final Map<String, String[]> params = request.getParameterMap();
    Map<String, String> res = Maps.newHashMap();

    for(AttributeConfiguration a : attributes) {
      if(!params.containsKey(a.getName())) {
        if(a.isRequired()) throw new BadRequestException("Missing attribute " + a.getName());
        continue;
      }

      String[] values = params.get(a.getName());

      if(values.length > 1) {
        throw new BadRequestException("Invalid repeated parameter " + a.getName());
      }

      String parsedValue;

      try {
        switch(a.getType()) {
          case INTEGER:
            parsedValue = Integer.valueOf(values[0]).toString();
            break;
          case BOOLEAN:
            parsedValue = Boolean.valueOf(values[0]).toString();
            break;
          case DECIMAL:
            parsedValue = Float.valueOf(values[0]).toString();
            break;
          default:
            parsedValue = values[0];
        }
      } catch(NumberFormatException e) {
        throw new BadRequestException("Invalid value " + values[0]);
      }

      if(a.hasValues() && !a.getValues().contains(parsedValue)) {
        throw new BadRequestException("Invalid value " + parsedValue);
      }

      res.put(a.getName(), parsedValue);
    }

    return res;
  }
}
