package org.obiba.agate.web.rest.config;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.obiba.agate.web.model.RealmConfigDtos;
import org.obiba.agate.web.rest.user.UserResource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Path("/config/realms")
@RequiresRoles("agate-administrator")
public class RealmsConfigResource {

  private final RealmConfigService realmConfigService;
  private final Dtos dtos;

  @Inject
  public RealmsConfigResource(RealmConfigService realmConfigService,
                              Dtos dtos) {
    this.realmConfigService = realmConfigService;
    this.dtos = dtos;
  }

  @GET
  public List<Agate.RealmConfigDto> get() {
    return realmConfigService.findAll().stream().map(dtos::asDto).collect(Collectors.toList());
  }

  @GET
  @Path("/realm-summaries")
  public List<Agate.RealmConfigSummaryDto> getSummaries() {
    return realmConfigService.findAll().stream().map(dtos::asSummaryDto).collect(Collectors.toList());
  }

  @POST
  public Response create(Agate.RealmConfigDto dto) {
    RealmConfig config = realmConfigService.findConfig(dto.getName());

    if(config != null) throw new BadRequestException("Config already exists: " + dto.getName());
    RealmConfig saved = realmConfigService.save(dtos.fromDto(dto));

    return Response
      .created(UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(UserResource.class)
        .build(saved.getId())).build();
  }

}
