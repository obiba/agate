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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.obiba.agate.domain.Group;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.rest.config.JerseyConfiguration;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
@RequiresRoles("AGATE_ADMIN")
@Path("/groups")
public class GroupsResource {

  private static final String CURRENT_USER_NAME = "_current";

  @Inject
  private UserService userService;

  @POST
  public Response create(@FormParam("name") String name, @FormParam("description") String description) {
    if(Strings.isNullOrEmpty(name)) throw new BadRequestException("Group name cannot be empty");
    Group group = userService.findGroup(name);
    if(group != null) throw new BadRequestException("Group already exists: " + name);

    userService.save(new Group(name, description));
    return Response
        .created(UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(GroupResource.class).build(group.getId()))
        .build();
  }

}
