package org.obiba.agate.web.rest.security;

import org.obiba.agate.service.RealmConfigService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Path("/realms")
public class RealmsResource {

  private final RealmConfigService realmConfigService;
  private final Dtos dtos;

  @Inject
  public RealmsResource(RealmConfigService realmConfigService, Dtos dtos)  {
    this.realmConfigService = realmConfigService;
    this.dtos = dtos;
  }


  @GET
  public List<Agate.RealmConfigSummaryDto> get() {
    return realmConfigService.findAll().stream().map(dtos::asSummaryDto).collect(Collectors.toList());
  }
}
