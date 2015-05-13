package org.obiba.agate.web.rest.config;

import java.io.IOException;
import java.security.KeyStoreException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.KeyStoreService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Lists;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/config")
public class ConfigurationResource {

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private KeyStoreService keyStoreService;

  @Inject
  private Dtos dtos;

  @GET
  @Timed
  public Agate.ConfigurationDto get() {
    return dtos.asDto(configurationService.getConfiguration());
  }

  /**
   * Get the Json representation of the form to be filled for submitting a join request.
   *
   * @return
   */
  @GET
  @Path("/join")
  @Produces(APPLICATION_JSON)
  @Timed
  public Response getJoinConfiguration() throws JSONException {
    Configuration config = configurationService.getConfiguration();
    JSONObject rval = new JSONObject();
    rval.put("schema", getJoinSchema(config));
    rval.put("definition", getJoinDefinition(config));
    return Response.ok(rval.toString()).build();
  }


  @PUT
  @Timed
  @RequiresRoles("agate-administrator")
  public Response create(@SuppressWarnings("TypeMayBeWeakened") Agate.ConfigurationDto dto) {
    configurationService.save(dtos.fromDto(dto));
    return Response.noContent().build();
  }

  @PUT
  @Path("/keystore/{name}/{alias}")
  @Timed
  @RequiresRoles("agate-administrator")
  public Response updateEncryptionKey(@PathParam("name") String name, @PathParam("alias") String alias,
    Agate.KeyForm keyForm) {
    if(keyForm.getKeyType() == Agate.KeyType.KEY_PAIR) {
      doCreateOrImportKeyPair(KeyStoreService.SYSTEM_KEY_STORE, "https", keyForm);
    } else {
      doImportCertificate(KeyStoreService.SYSTEM_KEY_STORE, "https", keyForm);
    }

    return Response.ok().build();
  }

  @GET
  @Path("/keystore/{name}/{alias}")
  @Timed
  @RequiresRoles("agate-administrator")
  public Response getEncryptionKeyCertificate(@PathParam("name") String name, @PathParam("alias") String alias)
    throws IOException, KeyStoreException {

    if("system".equals(name) && keyStoreService.getKeyStore(name).aliasExists(alias)) {
      return Response.ok(keyStoreService.getPEMCertificate(name, alias), MediaType.TEXT_PLAIN_TYPE)
        .header("Content-disposition", String.format("attachment; filename=%s-%s-certificate.pem", name, alias))
        .build();
    }

    return Response.status(Response.Status.NOT_FOUND).build();
  }

  private void doImportCertificate(String name, String alias, Agate.KeyForm keyForm) {
    keyStoreService.createOrUpdateCertificate(name, alias, keyForm.getPublicImport());
  }

  private void doCreateOrImportKeyPair(String name, String alias, Agate.KeyForm keyForm) {
    if(keyForm.hasPrivateForm() && keyForm.hasPublicForm()) {
      Agate.PublicKeyForm pkForm = keyForm.getPublicForm();
      keyStoreService
        .createOrUpdateCertificate(name, alias, keyForm.getPrivateForm().getAlgo(), keyForm.getPrivateForm().getSize(),
          pkForm.getName(), pkForm.getOrganizationalUnit(), pkForm.getOrganization(), pkForm.getLocality(),
          pkForm.getState(), pkForm.getCountry());
    } else if(keyForm.hasPrivateImport()) {
      doImportKeyPair(name, alias, keyForm);
    } else {
      throw new WebApplicationException("Missing private key", Response.Status.BAD_REQUEST);
    }
  }

  private void doImportKeyPair(String name, String alias, Agate.KeyForm keyForm) {
    if(keyForm.hasPublicForm()) {
      Agate.PublicKeyForm pkForm = keyForm.getPublicForm();
      keyStoreService.createOrUpdateCertificate(name, alias, keyForm.getPrivateImport(), pkForm.getName(),
        pkForm.getOrganizationalUnit(), pkForm.getOrganization(), pkForm.getLocality(), pkForm.getState(),
        pkForm.getCountry());
    } else if(keyForm.hasPublicImport()) {
      keyStoreService.createOrUpdateCertificate(name, alias, keyForm.getPrivateImport(), keyForm.getPublicImport());
    } else {
      throw new WebApplicationException("Missing public key", Response.Status.BAD_REQUEST);
    }
  }

  private JSONObject getJoinSchema(Configuration config) throws JSONException {
    JSONObject schema = new JSONObject();
    schema.putOnce("type", "object");
    JSONObject properties = new JSONObject();
    properties.put("email", newProperty("string", "Email") //
        .put("pattern", "^\\S+@\\S+$") //
        .put("description", "Email is required.") //
        .put("validationMessage", "Not a valid email.") //
    );
    properties.put("username", newProperty("string", "User Name").put("minLength", 3)
      .put("description", "If not specified, user name will be the user email."));
    properties.put("firstname", newProperty("string", "First Name"));
    properties.put("lastname", newProperty("string", "Last Name"));

    JSONArray required = new JSONArray(Lists.newArrayList("email"));

    if(config.hasUserAttributes()) {
      config.getUserAttributes().forEach(a -> {
        try {
          String type = a.getType().name().toLowerCase();
          JSONObject property = newProperty(type, a.getName());
          if(a.hasValues()) {
            a.getValues().forEach(e -> {
              try {
                property.append("enum", e);
              } catch(JSONException e1) {
                // ignored
              }
            });
          }
          properties.put(a.getName(), property);
          if(a.isRequired()) required.put(a.getName());
        } catch(JSONException e) {
          // ignored
        }
      });
    }

    schema.put("properties", properties);
    schema.put("required", required);
    return schema;
  }

  private JSONArray getJoinDefinition(Configuration config) throws JSONException {
    JSONArray definition = new JSONArray();
    definition.put("email");
    definition.put("username");
    definition.put("firstname");
    definition.put("lastname");
    if(config.hasUserAttributes()) {
      config.getUserAttributes().forEach(a -> definition.put(a.getName()));
    }
    return definition;
  }

  private JSONObject newProperty(String type, String title) throws JSONException {
    JSONObject property = new JSONObject();
    property.put("type", type);
    property.put("title", title);
    return property;
  }
}
