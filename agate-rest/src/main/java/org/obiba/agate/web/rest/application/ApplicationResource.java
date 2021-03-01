/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.application;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.obiba.agate.domain.Application;
import org.obiba.agate.service.ApplicationService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Path("/application/{id}")
public class ApplicationResource {

  @Inject
  private Dtos dtos;

  @Inject
  private ApplicationService applicationService;

  @GET
  @RequiresRoles("agate-administrator")
  public Agate.ApplicationDto getApplication(@PathParam("id")String id) {
    return dtos.asDto(applicationService.getApplication(id));
  }

  @GET
  @Path("/summary")
  @RequiresRoles("agate-user")
  public Agate.ApplicationDto getApplicationSummary(@PathParam("id")String id) {
    return dtos.asDto(applicationService.getApplication(id), true);
  }

  @PUT
  @RequiresRoles("agate-administrator")
  public Response updateApplication(@PathParam("id")String id, Agate.ApplicationDto dto) {
    Application application = applicationService.getApplication(id);
    application.setDescription(dto.getDescription());
    application.setRedirectURI(dto.getRedirectURI());
    application.setScopes(Lists.newArrayList());
    dto.getScopesList().forEach(s -> application.addScope(s.getName(), s.getDescription()));
    if (dto.hasKey()) application.setKey(applicationService.hashKey(dto.getKey()));
    if (dto.hasUserApprovedOnSignUp()) application.setUserApprovedOnSignUp(dto.getUserApprovedOnSignUp());

    applicationService.save(application);

    return Response.noContent().build();
  }

  @DELETE
  @RequiresRoles("agate-administrator")
  public Response delete(@PathParam("id")String id) {
    applicationService.delete(id);

    return Response.noContent().build();
  }
}
