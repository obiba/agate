package org.obiba.agate.web.rest.config;

import org.json.JSONException;
import org.obiba.agate.service.ConfigurationService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

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
                          @QueryParam("forEditing") @DefaultValue("false") boolean forEditing) throws JSONException {
    return Response.ok(configurationService.getRealmFormConfiguration(locale, forEditing).toString()).build();
  }

}
