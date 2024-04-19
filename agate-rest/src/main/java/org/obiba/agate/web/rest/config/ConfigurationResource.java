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

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.json.JSONException;
import org.obiba.agate.config.ClientConfiguration;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.KeyStoreService;
import org.obiba.agate.service.TotpService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.obiba.agate.web.rest.security.AuthorizationValidator;
import org.obiba.shiro.realm.ObibaRealm;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.security.KeyStoreException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Component
@Path("/config")
public class ConfigurationResource {

  private final ConfigurationService configurationService;

  private final KeyStoreService keyStoreService;

  private final Dtos dtos;

  private final ClientConfiguration clientConfiguration;

  private final AuthorizationValidator authorizationValidator;

  private final TotpService totpService;

  @Inject
  public ConfigurationResource(ConfigurationService configurationService,
                               KeyStoreService keyStoreService,
                               Dtos dtos,
                               ClientConfiguration clientConfiguration,
                               AuthorizationValidator authorizationValidator,
                               TotpService totpService) {
    this.configurationService = configurationService;
    this.keyStoreService = keyStoreService;
    this.dtos = dtos;
    this.clientConfiguration = clientConfiguration;
    this.authorizationValidator = authorizationValidator;
    this.totpService = totpService;
  }

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

    Agate.PublicConfigurationDto.Builder builder = Agate.PublicConfigurationDto.newBuilder()
      .setName(configuration.getName())
      .setJoinWithUsername(configuration.isJoinWithUsername())
      .addAllLanguages(configuration.getLocalesAsString());

    if (configuration.hasPublicUrl())
      builder.setPublicUrl(configuration.getPublicUrl());

    if (configuration.hasUserAttributes())
      configuration.getUserAttributes().forEach(attr -> builder.addUserAttributes(dtos.asDto(attr)));

    return builder.build();
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
  public Response getJoinConfiguration(@QueryParam("locale") @DefaultValue("en") String locale,
                                       @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader) throws JSONException, IOException {

    String application = Strings.isNullOrEmpty(authHeader) ? null : authorizationValidator.validateApplication(authHeader);
    return Response.ok(configurationService.getJoinConfiguration(locale, application).toString()).build();
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
  public Response getProfileConfiguration(@QueryParam("locale") @DefaultValue("en") String locale) throws JSONException, IOException {
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
    Configuration updatedConfiguration = dtos.fromDto(dto);
    configurationService.save(configurationService.applyInternalSettings(updatedConfiguration));
    return Response.noContent().build();
  }

  @PUT
  @Path("/otp")
  @Produces("text/plain")
  @RequiresRoles("agate-administrator")
  public Response enableOtp() {
    Configuration configuration = configurationService.getConfiguration();
    configuration.setSecretOtp(totpService.generateSecret());
    configurationService.save(configuration);
    Subject subject = SecurityUtils.getSubject();
    return Response.ok(totpService.getQrImageDataUri(subject.getPrincipal().toString(), configuration.getSecretOtp()), "text/plain").build();
  }

  @DELETE
  @Path("/otp")
  @RequiresRoles("agate-administrator")
  public Response disableOtp() {
    Configuration configuration = configurationService.getConfiguration();
    configuration.setSecretOtp(null);
    configurationService.save(configuration);
    return Response.ok().build();
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
