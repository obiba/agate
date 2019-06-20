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

import com.google.common.base.Strings;
import java.io.IOException;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserCredentials;
import org.obiba.agate.domain.UserStatus;
import org.obiba.agate.service.AuthorizationService;
import org.obiba.agate.service.NoSuchUserException;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Agate.AuthorizationDto;
import org.obiba.agate.web.model.Dtos;
import org.springframework.stereotype.Component;

@Component
@RequiresRoles("agate-administrator")
@Path("/user/{id}")
public class UserResource {

  @Inject
  protected UserService userService;

  @Inject
  protected AuthorizationService authorizationService;

  @Inject
  private Dtos dtos;

  @GET
  public Agate.UserDto get(@PathParam("id") String id) {
    User user = getUser(id);
    return dtos.asDto(user);
  }

  @PUT
  @Path("/password")
  public Response updatePassword(@PathParam("id") String id, @FormParam("password") String password) {
    userService.updateUserPassword(getUser(id), password);

    return Response.noContent().build();
  }

  @GET
  @Path("/authorizations")
  public List<AuthorizationDto> getAutorizations(@PathParam("id") String id) {
    return authorizationService.list(getUser(id).getName()).stream().map(dtos::asDto).collect(
      Collectors.toList());
  }

  @GET
  @Path("/authorization/{auth}")
  public Agate.AuthorizationDto getAutorization(@PathParam("auth") String auth) {
    if(Strings.isNullOrEmpty(auth)) throw new BadRequestException("Missing authorization ID");
    return dtos.asDto(authorizationService.get(auth));
  }

  @DELETE
  @Path("/authorization/{auth}")
  public Response deleteAutorization(@PathParam("auth") String auth) {
    if(Strings.isNullOrEmpty(auth)) throw new BadRequestException("Missing authorization ID");
    authorizationService.delete(auth);
    return Response.ok().build();
  }

  /**
   * Updates user properties
   */
  @PUT
  public Response updateUser(Agate.UserDto userDto) {
    userService.save(dtos.fromDto(userDto));
    return Response.noContent().build();
  }

  @PUT
  @Path("/role")
  public Response updateRole(@FormParam("role") @DefaultValue("agate-user") String role, @PathParam("id") String id) {
    User user = userService.getUser(id);
    user.setRole(role);
    userService.save(user);
    return Response.noContent().build();
  }

  @PUT
  @Path("/status")
  public Response updateStatus(@FormParam("status") String status, @PathParam("id") String id) {
    User user = userService.getUser(id);
    userService.updateUserStatus(user, UserStatus.valueOf(status.toUpperCase()));

    return Response.noContent().build();
  }

  @PUT
  @Path("/reset_password")
  public Response resetPassword(@PathParam("id") String id) throws IOException {
    User user = userService.getUser(id);

    if(user == null) throw NoSuchUserException.withId(id);

    UserCredentials userCredentials = userService.findUserCredentials(user.getName());

    if(userCredentials == null) throw new BadRequestException("User has no defined credentials.");

    userService.resetPassword(user);

    return Response.noContent().build();
  }

  @DELETE
  public Response delete(@PathParam("id") String id) {
    try {
      User user = userService.getUser(id);
      userService.delete(user);
    } catch(NoSuchUserException e) {
      // ignore
    }
    return Response.noContent().build();
  }

  protected User getUser(String id) {
    return userService.getUser(id);
  }

}
