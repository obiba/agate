package org.obiba.agate.web.rest.config;

import org.json.JSONException;
import org.obiba.agate.service.ConfigurationService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.io.IOException;

@Component
@Scope("request")
@Path("/config/realm-form")
public class RealmConfigFormResource {

  private final ConfigurationService configurationService;

  @Inject
  public RealmConfigFormResource(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }

  @GET
  public Response getForm(@QueryParam("locale") @DefaultValue("en") String locale,
                          @QueryParam("forEditing") @DefaultValue("false") boolean forEditing) throws JSONException, IOException {
    return Response.ok(configurationService.getRealmFormConfiguration(locale, forEditing).toString()).build();
  }

}
