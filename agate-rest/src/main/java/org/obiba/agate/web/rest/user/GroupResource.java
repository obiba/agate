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
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.obiba.agate.domain.Group;
import org.obiba.agate.service.NoSuchGroupException;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.springframework.stereotype.Component;

@Component
@RequiresRoles("AGATE_ADMIN")
@Path("/group/{id}")
public class GroupResource {

  @Inject
  private UserService userService;

  @Inject
  private Dtos dtos;

  @PathParam("id")
  private String id;

  @GET
  public Agate.GroupDto get() {
    Group group = userService.getGroup(id);
    return dtos.asDto(group);
  }

  @PUT
  public Response update(@FormParam("description") String description) {
    Group group = userService.getGroup(id);
    group.setDescription(description);
    userService.save(group);
    return Response.noContent().build();
  }

  @DELETE
  public Response delete() {
    try {
      Group group = userService.getGroup(id);
      userService.delete(group);
    } catch (NoSuchGroupException e) {
      // ignore
    }
    return Response.noContent().build();
  }

}
