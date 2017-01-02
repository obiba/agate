/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserStatus;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.obiba.agate.web.rest.config.JerseyConfiguration;
import org.springframework.stereotype.Component;

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
  public List<Agate.UserDto> get(@QueryParam("status") String status) {
    ImmutableList.Builder<Agate.UserDto> builder = ImmutableList.builder();
    List<User> users = status != null ? userService.findUsers(UserStatus.valueOf(status.toUpperCase())) : userService.findUsers();

    for(User user : users) {
      builder.add(dtos.asDto(user));
    }

    return builder.build();
  }

  @POST
  public Response create(Agate.UserCreateFormDto userCreateFormDto) {
    Agate.UserDto userDto = userCreateFormDto.getUser();
    String username = userDto.getName();

    if (new EmailValidator().isValid(username, null)) throw new BadRequestException("username can not be an email address.");

    User user = userService.findUser(username);

    if(user != null) throw new BadRequestException("User already exists: " + username);

    user = userService.findUserByEmail(userDto.getEmail());

    if(user != null) throw new BadRequestException("Email already in use: " + user.getEmail());

    if(CURRENT_USER_NAME.equals(username)) throw new BadRequestException("Reserved user name: " + CURRENT_USER_NAME);

    user = userService.createUser(dtos.fromDto(userDto), userCreateFormDto.getPassword());

    return Response
      .created(UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(UserResource.class).build(user.getId())).build();
  }
}
