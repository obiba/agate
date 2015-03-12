/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.user;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.obiba.agate.web.rest.config.JerseyConfiguration;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

@Component
@RequiresRoles("agate-administrator")
@Path("/users")
public class UsersResource {

  private static final String CURRENT_USER_NAME = "_current";

  @Inject
  private UserService userService;

  @Inject
  private Dtos dtos;

  @GET
  public List<Agate.UserDto> get() {
    ImmutableList.Builder<Agate.UserDto> builder = ImmutableList.builder();
    for(User user : userService.findUsers()) {
      builder.add(dtos.asDto(user));
    }
    return builder.build();
  }

  @POST
  public Response create(@FormParam("username") String username, @FormParam("realm") String realm,
    @FormParam("role") @DefaultValue("agate-user") String role, @FormParam("firstname") String firstName,
    @FormParam("lastname") String lastName, @FormParam("email") String email, @FormParam("group") List<String> groups) {
    if(Strings.isNullOrEmpty(username)) throw new BadRequestException("User name cannot be empty");
    User user = userService.findUser(username);
    if(user != null) throw new BadRequestException("User already exists: " + username);
    if(CURRENT_USER_NAME.equals(username)) throw new BadRequestException("Reserved user name: " + CURRENT_USER_NAME);

    user = User.newBuilder().name(username).realm(realm).role(role).firstName(firstName).lastName(lastName).email(email)
      .groups(groups).build();
    userService.save(user);
    return Response
      .created(UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(UserResource.class).build(user.getId())).build();
  }

}
