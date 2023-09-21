package org.obiba.agate.web.rest.config;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/config/realms")
@RequiresRoles("agate-administrator")
public class RealmsConfigResource {

  private final RealmConfigService realmConfigService;
  private final Dtos dtos;

  @Inject
  public RealmsConfigResource(RealmConfigService realmConfigService, Dtos dtos) {
    this.realmConfigService = realmConfigService;
    this.dtos = dtos;
  }

  @GET
  public List<Agate.RealmConfigDto> get() {
    return realmConfigService.findAll().stream().map(dtos::asDto).collect(Collectors.toList());
  }

  @GET
  @Path("/summaries")
  public List<Agate.RealmConfigSummaryDto> getSummaries() {
    return realmConfigService.findAll().stream().map(dtos::asSummaryDto).collect(Collectors.toList());
  }

  @POST
  public Response create(Agate.RealmConfigDto dto) {
    RealmConfig config = realmConfigService.findConfig(dto.getName());

    if(config != null) throw new BadRequestException("Config already exists: " + dto.getName());
    if(Pattern.compile("[^0-9A-Za-z-_]").matcher(dto.getName()).find()) throw new BadRequestException("Name should avoid special characters. Use alphanumerical characters instead (hyphens and underscores are allowed).");

    RealmConfig saved = realmConfigService.save(dtos.fromDto(dto));

    return Response.created(UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(RealmConfigResource.class).build(saved.getId())).build();
  }

}
