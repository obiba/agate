/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
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
import javax.ws.rs.BadRequestException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.obiba.agate.domain.Group;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.obiba.agate.web.rest.config.JerseyConfiguration;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

@Component
@RequiresRoles("agate-administrator")
@Path("/groups")
public class GroupsResource {

  @Inject
  private UserService userService;

  @Inject
  private Dtos dtos;

  @POST
  public Response create(Agate.GroupDto groupDto) {
    Group group = userService.findGroup(groupDto.getName());

    if(group != null) throw new BadRequestException("Group already exists: " + group);

    group = dtos.fromDto(groupDto);
    userService.save(group);

    return Response
        .created(UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(GroupResource.class).build(group.getId()))
        .build();
  }

  @GET
  public List<Agate.GroupDto> get() {
    ImmutableList.Builder<Agate.GroupDto> builder = ImmutableList.builder();

    for(Group group : userService.findGroups()) {
      builder.add(dtos.asDto(group));
    }

    return builder.build();
  }

}
