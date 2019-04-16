package org.obiba.agate.web.rest.config;

import org.obiba.agate.service.RealmConfigService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/config/realm/{name}")
public class RealmConfigResource {

  private final RealmConfigService realmConfigService;

  private final Dtos dtos;

  @Inject
  public RealmConfigResource(RealmConfigService realmConfigService, Dtos dtos) {
    this.realmConfigService = realmConfigService;
    this.dtos = dtos;
  }

  @GET
  public Agate.RealmConfigDto get(@PathParam("name") String name) {
    return dtos.asDto(realmConfigService.getConfig(name));
  }

  @GET
  @Path("/usernames")
  public List<String> getUsernames(@PathParam("name") String name) {
    return realmConfigService.getUsernames(name);
  }

  @PUT
  public Response update(@PathParam("name") String name, Agate.RealmConfigDto dto) {
    realmConfigService.getConfig(dto.getName());
    realmConfigService.save(dtos.fromDto(dto));
    return Response.noContent().build();
  }

  @PUT
  @Path("/active")
  public Response activate(@PathParam("name") String name) {
    realmConfigService.activate(name);
    return Response.noContent().build();
  }

  @DELETE
  @Path("/active")
  public Response deactivate(@PathParam("name") String name) {
    realmConfigService.deactivate(name);
    return Response.noContent().build();
  }

  @PUT
  @Path("/groups")
  public Response updateGroups(@PathParam("name") String name, @QueryParam("name") List<String> names) {
    realmConfigService.updateGroups(name, names);
    return Response.noContent().build();
  }

  @DELETE
  public Response delete(@PathParam("name") String name) {
    realmConfigService.delete(name);
    return Response.noContent().build();
  }
}
