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

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.obiba.agate.domain.User;
import org.obiba.agate.web.model.Agate;
import org.springframework.stereotype.Component;

@Component
@RequiresRoles("agate-user")
@Path("/user/_current")
public  class CurrentUserResource extends AbstractUserResource {

  @PUT
  public Response updateUser(Agate.UserDto userDto) {
    User current = getUser();
    if (!current.getId().equals(userDto.getId())) return Response.status(Response.Status.FORBIDDEN).build();
    return super.updateUser(userDto);
  }

  @Override
  protected User getUser() {
    return userService.getCurrentUser();
  }
}
