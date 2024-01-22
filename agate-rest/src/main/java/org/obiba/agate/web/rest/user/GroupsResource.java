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

import java.util.List;

import javax.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.obiba.agate.domain.Group;
import org.obiba.agate.service.GroupService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.obiba.agate.web.rest.config.JerseyConfiguration;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

@Component
@RequiresRoles("agate-administrator")
@Path("/groups")
public class GroupsResource {

  private final GroupService groupService;

  private final Dtos dtos;

  @Inject
  public GroupsResource(GroupService groupService, Dtos dtos) {
    this.groupService = groupService;
    this.dtos = dtos;
  }

  @POST
  public Response create(Agate.GroupDto groupDto) {
    Group group = groupService.findGroup(groupDto.getName());

    if(group != null) throw new BadRequestException("Group already exists: " + group);

    group = groupService.save(dtos.fromDto(groupDto));

    return Response
        .created(UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(GroupResource.class).build(group.getId()))
        .build();
  }

  @GET
  public List<Agate.GroupDto> get() {
    ImmutableList.Builder<Agate.GroupDto> builder = ImmutableList.builder();

    for(Group group : groupService.findGroups()) {
      builder.add(dtos.asDto(group));
    }

    return builder.build();
  }

}
