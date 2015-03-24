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

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.obiba.agate.domain.User;
import org.obiba.agate.security.AgateUserRealm;
import org.obiba.agate.security.Roles;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.rest.config.JerseyConfiguration;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

/**
 * Public resource for user join requests. Default realm is {@link org.obiba.agate.security.AgateUserRealm}.
 */
@Component
@Path("/users/_join")
public class UsersJoinResource {

  private static final String CURRENT_USER_NAME = "_current";

  @Inject
  private UserService userService;

  @POST
  public Response create(@FormParam("username") String username, @FormParam("firstname") String firstName,
    @FormParam("lastname") String lastName, @FormParam("email") String email) {
    if(Strings.isNullOrEmpty(username)) throw new BadRequestException("User name cannot be empty");
    User user = userService.findUser(username);
    if(user != null) throw new BadRequestException("User already exists: " + username);
    if(CURRENT_USER_NAME.equals(username)) throw new BadRequestException("Reserved user name: " + CURRENT_USER_NAME);

    user = User.newBuilder().name(username).realm(AgateUserRealm.AGATE_REALM).role(Roles.AGATE_USER).pending()
      .firstName(firstName).lastName(lastName).email(email).build();
    userService.save(user);

    return Response
      .created(UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(UserResource.class).build(user.getId())).build();
  }

}
