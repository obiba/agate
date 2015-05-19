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

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.obiba.agate.domain.User;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;

public abstract class AbstractUserResource {

  @Inject
  protected UserService userService;

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

  /**
   * Updates user properties
   */
  protected Response updateUser(Agate.UserDto userDto) {
    User user = userService.findUserByEmail(userDto.getEmail());

    if(user != null && !user.getName().equals(userDto.getName())) throw new BadRequestException("Email already in user: " + user.getEmail());

    user = dtos.fromDto(userDto);
    userService.save(user);

    return Response.noContent().build();
  }

  /**
   * Get the {@link org.obiba.agate.domain.User} that is being processed.
   *
   * @return
   */
  protected abstract User getUser();

}
