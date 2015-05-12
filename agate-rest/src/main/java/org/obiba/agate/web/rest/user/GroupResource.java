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

import com.google.common.collect.Sets;

@Component
@RequiresRoles("agate-administrator")
@Path("/group/{id}")
public class GroupResource {

  @Inject
  private UserService userService;

  @Inject
  private Dtos dtos;

  @GET
  public Agate.GroupDto get(@PathParam("id") String id) {
    Group group = userService.getGroup(id);
    return dtos.asDto(group);
  }

  @PUT
  public Response update(@PathParam("id") String id, Agate.GroupDto groupDto) {
    Group existingGroup = userService.findGroup(groupDto.getName());

    if(existingGroup != null && !existingGroup.getId().equals(id)) {
      throw new BadRequestException("Group already exists: " + existingGroup);
    }

    final Group group = userService.getGroup(id);
    group.setName(groupDto.getName());
    group.setDescription(groupDto.getDescription());
    group.setApplications(Sets.newHashSet(groupDto.getApplicationsList()));

    userService.save(group);

    return Response.noContent().build();
  }

  @DELETE
  public Response delete(@PathParam("id") String id) {
    try {
      Group group = userService.getGroup(id);
      userService.delete(group);
    } catch (NoSuchGroupException e) {
      // ignore
    }

    return Response.noContent().build();
  }
}
