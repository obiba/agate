package org.obiba.agate.web.rest.config;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;

import com.codahale.metrics.annotation.Timed;

@Path("/config")
public class ConfigurationResource {

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private Dtos dtos;

  @GET
  @Timed
  public Agate.ConfigurationDto get() {
    return dtos.asDto(configurationService.getConfiguration());
  }

  @PUT
  @Timed
  public Response create(@SuppressWarnings("TypeMayBeWeakened") Agate.ConfigurationDto dto) {
    configurationService.save(dtos.fromDto(dto));
    return Response.noContent().build();
  }

}
