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
import org.obiba.agate.domain.User;
import org.obiba.agate.service.GroupService;
import org.obiba.agate.service.NoSuchGroupException;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiresRoles("agate-administrator")
@Path("/group/{id}")
public class GroupResource {

  private final GroupService groupService;

  private final UserService userService;

  private final Dtos dtos;

  @Inject
  public GroupResource(GroupService groupService, UserService userService, Dtos dtos) {
    this.groupService = groupService;
    this.userService = userService;
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

  @DELETE
  @Path("/users")
  public Response removeUsers(@PathParam("id") String id, @QueryParam("names") List<String> names) {
    if (names == null || names.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    List<User> usersToRemove = groupService.getUsers(id).stream().filter(u -> names.contains(u.getName())).toList();
    usersToRemove.forEach(user -> {
      user.setGroups(user.getGroups().stream().filter(g -> !g.equals(id)).collect(Collectors.toSet()));
      userService.save(user);
    });
    return Response.noContent().build();
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
