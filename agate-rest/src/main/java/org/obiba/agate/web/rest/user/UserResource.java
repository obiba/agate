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
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserStatus;
import org.obiba.agate.service.NoSuchUserException;
import org.obiba.agate.web.model.Dtos;
import org.springframework.stereotype.Component;

@Component
@RequiresRoles("agate-administrator")
@Path("/user/{id}")
public class UserResource extends AbstractUserResource {

  @Inject
  private Dtos dtos;

  @PathParam("id")
  private String id;

  @PUT
  @Path("/role")
  public Response updateRole(@FormParam("role") @DefaultValue("agate-user") String role) {
    User user = userService.getUser(id);
    user.setRole(role);
    userService.save(user);
    return Response.noContent().build();
  }

  @PUT
  @Path("/status")
  public Response updateStatus(@FormParam("status") String status) {
    User user = userService.getUser(id);
    user.setStatus(UserStatus.valueOf(status.toUpperCase()));
    userService.save(user);
    return Response.noContent().build();
  }

  @DELETE
  public Response delete() {
    try {
      User user = userService.getUser(id);
      userService.delete(user);
    } catch(NoSuchUserException e) {
      // ignore
    }
    return Response.noContent().build();
  }

  @Override
  protected User getUser() {
    return userService.getUser(id);
  }

}
