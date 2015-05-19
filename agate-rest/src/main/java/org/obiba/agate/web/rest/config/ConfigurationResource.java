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
import org.json.JSONException;
import org.obiba.agate.config.ClientConfiguration;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.KeyStoreService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import com.codahale.metrics.annotation.Timed;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/config")
public class ConfigurationResource {

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private KeyStoreService keyStoreService;

  @Inject
  private Dtos dtos;

  @Inject
  private ClientConfiguration clientConfiguration;

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
    return Response.ok(configurationService.getJoinConfiguration().toString()).build();
  }

  @GET
  @Path("/client")
  @Produces(APPLICATION_JSON)
  public ClientConfiguration getClientConfiguration() throws JSONException {
    return clientConfiguration;
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

}
