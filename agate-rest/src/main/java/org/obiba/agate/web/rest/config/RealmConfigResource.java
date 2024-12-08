package org.obiba.agate.web.rest.config;

import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
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
  public Agate.RealmConfigDto get(@PathParam("name") String name,
                                  @QueryParam("includeUsers") @DefaultValue("false") boolean includeUsers) {
    Agate.RealmConfigDto.Builder builder = dtos.asDtoBuilder(realmConfigService.getConfig(name));
    if (includeUsers) builder.addAllUsers(getUsers(name));
    return builder.build();
  }

  @GET
  @Path("/users")
  public List<Agate.UserSummaryDto> getUsers(@PathParam("name") String name) {
    return realmConfigService.getUsers(name).stream().map(dtos::asSummaryDto).collect(Collectors.toList());
  }

  @PUT
  public Response update(@PathParam("name") String name, Agate.RealmConfigDto dto) {
    RealmConfig old = realmConfigService.getConfig(dto.getName());
    RealmConfig config = dtos.fromDto(dto);

    if (!old.getType().equals(config.getType()) && realmConfigService.getUsernames(name).size() > 0)
      throw new BadRequestException("Cannot change type if realm has users associated to it.");

    realmConfigService.save(config);
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
