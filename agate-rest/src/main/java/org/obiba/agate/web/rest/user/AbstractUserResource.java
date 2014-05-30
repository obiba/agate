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

import com.google.common.base.Strings;

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
  public Response update(@FormParam("firstname") String firstName, @FormParam("lastname") String lastName,
      @FormParam("email") String email) {
    User user = getUser();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    userService.save(user);
    return Response.noContent().build();
  }

  @PUT
  @Path("/password")
  public Response updatePassword(@FormParam("password") String password) {
    if(Strings.isNullOrEmpty(password)) throw new BadRequestException("User password cannot be empty");
    User user = getUser();
    user.setPassword(userService.hashPassword(password));
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
