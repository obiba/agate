package org.obiba.agate.web.rest.config;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.obiba.agate.service.config.AgateConfigService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;

import com.codahale.metrics.annotation.Timed;

@Path("/config")
public class AgateConfigResource {

  @Inject
  private AgateConfigService agateConfigService;

  @Inject
  private Dtos dtos;

  @GET
  @Timed
  public Agate.AgateConfigDto get() {
    return dtos.asDto(agateConfigService.getConfig());
  }

  @PUT
  @Timed
  public Response create(@SuppressWarnings("TypeMayBeWeakened") Agate.AgateConfigDto dto) {
    agateConfigService.save(dtos.fromDto(dto));
    return Response.noContent().build();
  }

  @GET
  @Path("/languages")
  @Timed
  public Map<String, String> getAvailableLanguages() {
    //TODO support user locale (http://jira.obiba.org/jira/browse/MICASERVER-39)
    Locale locale = Locale.ENGLISH;
    return Arrays.asList(Locale.getISOLanguages()).stream()
        .collect(Collectors.toMap(lang -> lang, lang -> new Locale(lang).getDisplayLanguage(locale)));
  }

}
