/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.config;

import java.io.IOException;
import java.security.KeyStoreException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.json.JSONException;
import org.obiba.agate.config.ClientConfiguration;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.KeyStoreService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;

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
  @RequiresRoles("agate-user")
  public Agate.ConfigurationDto get() {
    return dtos.asDto(configurationService.getConfiguration());
  }

  @GET
  @Path("/_public")
  public Agate.PublicConfigurationDto getPublic() {
    Configuration configuration = configurationService.getConfiguration();

    return Agate.PublicConfigurationDto.newBuilder()
      .setName(configuration.getName())
      .addAllLanguages(configuration.getLocalesAsString())
      .build();
  }

  @GET
  @Path("/style.css")
  @Produces("text/css")
  public Response getStyle() {
    return Response
      .ok(configurationService.getConfiguration().getStyle(), "text/css")
      .header("Content-Disposition", "attachment; filename=\"style.css\"").build();
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
  public Response getJoinConfiguration(@QueryParam("locale") String locale) throws JSONException, IOException {
    return Response.ok(configurationService.getJoinConfiguration(locale).toString()).build();
  }

  /**
   * Get the Json representation of the form to be filled for updating a user profile.
   *
   * @return
   */
  @GET
  @Path("/profile")
  @Produces(APPLICATION_JSON)
  @Timed
  public Response getProfileConfiguration(@QueryParam("locale") String locale) throws JSONException, IOException {
    return Response.ok(configurationService.getProfileConfiguration(locale).toString()).build();
  }

  @GET
  @Path("/client")
  @Produces(APPLICATION_JSON)
  @Timed
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

  @GET
  @Path("/languages")
  @Timed
  @RequiresAuthentication
  public Map<String, String> getAvailableLanguages() {
    Locale locale = Locale.ENGLISH;
    return Arrays.asList(Locale.getISOLanguages()).stream()
      .collect(Collectors.toMap(lang -> lang, lang -> new Locale(lang).getDisplayLanguage(locale)));
  }

  @GET
  @Path("/i18n/{locale}.json")
  @Produces("application/json")
  public Response getTranslation(@PathParam("locale") String locale, @QueryParam("default") boolean _default) throws IOException {
    return Response.ok(
      configurationService.getTranslations(locale, _default).toString(), "application/json").build();
  }
}
