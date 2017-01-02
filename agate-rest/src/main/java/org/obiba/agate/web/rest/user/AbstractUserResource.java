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
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.agate.domain.User;
import org.obiba.agate.service.AuthorizationService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;

import com.google.common.base.Strings;

public abstract class AbstractUserResource {

  @Inject
  protected UserService userService;

  @Inject
  protected AuthorizationService authorizationService;

  @Inject
  private Dtos dtos;

  @GET
  public Agate.UserDto get() {
    User user = getUser();
    return dtos.asDto(user);
  }

  @PUT
  @Path("/password")
  public Response updatePassword(@FormParam("password") String password) {
    userService.updateUserPassword(getUser(), password);

    return Response.noContent().build();
  }

  @GET
  @Path("/authorizations")
  public List<Agate.AuthorizationDto> getAutorizations() {
    return authorizationService.list(getUser().getName()).stream().map(dtos::asDto).collect(Collectors.toList());
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
  protected Response updateUser(Agate.UserDto userDto) {
    userService.save(dtos.fromDto(userDto));
    return Response.noContent().build();
  }

  /**
   * Get the {@link org.obiba.agate.domain.User} that is being processed.
   *
   * @return
   */
  protected abstract User getUser();

}
