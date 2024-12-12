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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import jakarta.inject.Inject;

import com.codahale.metrics.annotation.Timed;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserStatus;
import org.obiba.agate.service.UserCsvService;
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

  private static final List<String> RESERVED_USER_NAMES = Lists.newArrayList("_current", "administrator");

  @Inject
  private UserService userService;

  @Inject
  private Dtos dtos;

  @Inject
  private UserCsvService userCsvService;

  @GET
  public List<Agate.UserDto> get(@QueryParam("status") String status) {
    ImmutableList.Builder<Agate.UserDto> builder = ImmutableList.builder();
    List<User> users = status != null ? userService.findUsers(UserStatus.valueOf(status.toUpperCase())) : userService.findUsers();

    for(User user : users) {
      builder.add(dtos.asDto(user));
    }

    return builder.build();
  }

  @GET
  @Path("/find")
  @RequiresRoles("agate-administrator")
  public Agate.UserDto getUserId(@QueryParam("q") String searchTerm) {

    User user = userService.findUser(searchTerm);
    if(user == null) user = userService.findUserByEmail(searchTerm);

    if (user == null)  throw new BadRequestException(String.format("Cannot find user \"%s\"", searchTerm));

    return dtos.asDto(user);
  }

  @POST
  public Response create(Agate.UserCreateFormDto userCreateFormDto) {
    Agate.UserDto userDto = userCreateFormDto.getUser();
    String username = userDto.getName().trim();
    
    User user = userService.findUser(username);

    if(user != null) throw new BadRequestException("User already exists: " + username);

    user = userService.findUserByEmail(userDto.getEmail().trim());

    if(user != null) throw new BadRequestException("Email already in use: " + user.getEmail());

    if(RESERVED_USER_NAMES.contains(username)) throw new BadRequestException("Reserved user name");

    if (AgateRealm.AGATE_USER_REALM.name().equals(userDto.getRealm()) && Strings.isNullOrEmpty(userCreateFormDto.getPassword()))
      throw new BadRequestException("User requires a password");

    user = userService.createUser(dtos.fromDto(userDto), userCreateFormDto.getPassword());

    return Response
      .created(UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(UserResource.class).build(user.getId())).build();
  }

  @GET
  @Timed
  @Path("/_csv")
  @Produces("text/csv")
  public Response csv(@QueryParam("status") String status) throws IOException {
    List<User> users = status != null ? userService.findUsers(UserStatus.valueOf(status.toUpperCase())) : userService.findUsers();

    ByteArrayOutputStream csvOutput = userCsvService.toCsv(users);
    return Response.ok(csvOutput.toByteArray(), "text/csv").header("Content-Disposition", "attachment; filename=\"users.csv\"").build();
  }
}
