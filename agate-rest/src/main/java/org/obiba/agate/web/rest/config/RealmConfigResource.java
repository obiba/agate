package org.obiba.agate.web.rest.config;

import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/config/realm/{name}")
public class RealmConfigResource {

  private final RealmConfigService realmConfigService;
  private final Dtos dtos;

  @Inject
  public RealmConfigResource(RealmConfigService realmConfigService,
                              Dtos dtos) {
    this.realmConfigService = realmConfigService;
    this.dtos = dtos;
  }

  @GET
  public Agate.RealmConfigDto get(@PathParam("name") String name) {
    return dtos.asDto(realmConfigService.getConfig(name));
  }

  @PUT
  public Response update(@PathParam("name") String name, Agate.RealmConfigDto dto) {
    realmConfigService.getConfig(dto.getName());
    realmConfigService.save(dtos.fromDto(dto));
    return Response.noContent().build();
  }

  @DELETE
  public Response delete(@PathParam("name") String name) {
    RealmConfig config = realmConfigService.findConfig(name);
    if (config != null) {
      realmConfigService.delete(config);
    }

    return Response.noContent().build();
  }
}
