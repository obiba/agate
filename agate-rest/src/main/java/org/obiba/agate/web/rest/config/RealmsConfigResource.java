package org.obiba.agate.web.rest.config;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.rest.user.UserResource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Component
@Path("/config/realms")
@RequiresRoles("agate-administrator")
public class RealmsConfigResource {

  private final RealmConfigService realmConfigService;

  @Inject
  public RealmsConfigResource(RealmConfigService realmConfigService) {
    this.realmConfigService = realmConfigService;
  }

  @POST
  public Response create(Agate.RealmConfigDto dto) {
    RealmConfig config = realmConfigService.findConfig(dto.getName());

    if(config != null) throw new BadRequestException("Config already exists: " + dto.getName());

    return Response
      .created(UriBuilder.fromPath(JerseyConfiguration.WS_ROOT).path(UserResource.class)
        .build(realmConfigService.save(config).getId())).build();
  }


}
