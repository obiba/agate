/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.application;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
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

@Component
@RequiresRoles("agate-administrator")
@Path("/application/{id}")
public class ApplicationResource {

  @Inject
  private Dtos dtos;

  @Inject
  private ApplicationService applicationService;

  @GET
  public Agate.ApplicationDto getApplication(@PathParam("id")String id) {
    return dtos.asDto(applicationService.getApplication(id));
  }

  @PUT
  public Response updateApplication(@PathParam("id")String id, Agate.ApplicationDto dto) {
    Application application = applicationService.getApplication(id);

    if (!application.getName().equals(dto.getName()) && applicationService.findByName(dto.getName()) != null) {
      throw new BadRequestException("Application with name " + dto.getName() + " already exists");
    }

    application.setDescription(dto.getDescription());
    application.setName(dto.getName());
    application.setKey(applicationService.hashKey(dto.getKey()));

    applicationService.save(application);

    return Response.noContent().build();
  }

  @DELETE
  public Response delete(@PathParam("id")String id) {
    applicationService.delete(id);

    return Response.noContent().build();
  }
}
