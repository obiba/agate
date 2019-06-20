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

import com.google.common.collect.Sets;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.obiba.agate.domain.Group;
import org.obiba.agate.service.GroupService;
import org.obiba.agate.service.NoSuchGroupException;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiresRoles("agate-administrator")
@Path("/group/{id}")
public class GroupResource {

  private final GroupService groupService;

  private final Dtos dtos;

  @Inject
  public GroupResource(GroupService groupService, Dtos dtos) {
    this.groupService = groupService;
    this.dtos = dtos;
  }

  @GET
  public Agate.GroupDto get(@PathParam("id") String id,
                            @QueryParam("includeUsers") @DefaultValue("false") boolean includeUsers) {
    Agate.GroupDto.Builder builder = dtos.asDtoBuilder(groupService.getGroup(id));
    if (includeUsers) builder.addAllUsers(getUsers(id));
    return builder.build();
  }

  @GET
  @Path("/users")
  public List<Agate.UserSummaryDto> getUsers(@PathParam("id") String id) {
    return groupService.getUsers(id).stream().map(dtos::asSummaryDto).collect(Collectors.toList());
  }

  @PUT
  public Response update(@PathParam("id") String id, Agate.GroupDto groupDto) {
    final Group group = groupService.getGroup(id);
    group.setDescription(groupDto.getDescription());
    group.setApplications(Sets.newHashSet(groupDto.getApplicationsList()));

    groupService.save(group);

    return Response.noContent().build();
  }

  @DELETE
  public Response delete(@PathParam("id") String id) {
    try {
      Group group = groupService.getGroup(id);
      groupService.delete(group);
    } catch (NoSuchGroupException e) {
      // ignore
    }

    return Response.noContent().build();
  }
}
