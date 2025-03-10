/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.util.ByteSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.config.MetricsConfiguration;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.LocalizedString;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.event.AgateConfigUpdatedEvent;
import org.obiba.agate.repository.AgateConfigRepository;
import org.obiba.agate.service.support.*;
import org.obiba.core.translator.JsonTranslator;
import org.obiba.core.translator.PrefixedValueTranslator;
import org.obiba.core.translator.TranslationUtils;
import org.obiba.core.translator.Translator;
import org.obiba.shiro.crypto.LegacyAesCipherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.jayway.jsonpath.Configuration.defaultConfiguration;

@Component
public class ConfigurationService {

  private static final Logger log = LoggerFactory.getLogger(ConfigurationService.class);

  private static final com.jayway.jsonpath.Configuration conf = defaultConfiguration();

  private final AgateConfigRepository agateConfigRepository;

  private final RealmConfigService realmConfigService;

  private final EventBus eventBus;

  private final Environment env;

  private final ApplicationContext applicationContext;

  private final ObjectMapper objectMapper;

  private final AesCipherService cipherService;

  private Configuration cachedConfiguration;

  private final LegacyAesCipherService legacyCipherService = new LegacyAesCipherService();

  @Inject
  public ConfigurationService(
    AgateConfigRepository agateConfigRepository,
    RealmConfigService realmConfigService,
    EventBus eventBus,
    Environment env,
    ApplicationContext applicationContext,
    ObjectMapper objectMapper) {
    this.agateConfigRepository = agateConfigRepository;
    this.realmConfigService = realmConfigService;
    this.eventBus = eventBus;
    this.env = env;
    this.applicationContext = applicationContext;
    this.objectMapper = objectMapper;
    this.cipherService = new AesCipherService();
  }

  /**
   * Get the http server context path, if configured.
   *
   * @return
   */
  public String getContextPath() {
    String contextPath = env.getProperty("server.context-path", "");
    return Strings.isNullOrEmpty(contextPath) ? env.getProperty("server.servlet.context-path", "") : contextPath;
  }

  /**
   * Be flexible with entry point as agate could be accessed from different host name.
   *
   * @param request
   * @return
   */
  public String getBaseURL(HttpServletRequest request) {
    String host = request.getHeader("Host");
    String baseURL;
    if (Strings.isNullOrEmpty(host))
      baseURL = getPublicUrl();
    else {
      // enforce https scheme for non localhost connection
      String scheme = host.startsWith("localhost:") || host.startsWith("127.0.0.1:") ? request.getScheme() : "https";
      if (Strings.isNullOrEmpty(getContextPath()))
        baseURL = String.format("%s://%s", scheme, host);
      else
        baseURL = String.format("%s://%s%s", scheme, host, getContextPath());
    }
    return baseURL;
  }

  public Configuration getConfiguration() {
    if (cachedConfiguration == null) {
      cachedConfiguration = getOrCreateConfiguration();
      cachedConfiguration.setContextPath(getContextPath());
      if (cachedConfiguration.getLocales().isEmpty())
        cachedConfiguration.getLocales().add(Configuration.DEFAULT_LOCALE);
    }
    return cachedConfiguration;
  }

  public void save(@Valid Configuration configuration) {
    Configuration savedConfiguration = getOrCreateConfiguration();
    BeanUtils
      .copyProperties(configuration, savedConfiguration, "id", "version", "createdBy", "createdDate", "lastModifiedBy",
        "lastModifiedDate", "secretKey", "agateVersion");
    if (configuration.getAgateVersion() != null) savedConfiguration.setAgateVersion(configuration.getAgateVersion());
    agateConfigRepository.save(savedConfiguration);
    cachedConfiguration = null;
    eventBus.post(new AgateConfigUpdatedEvent(savedConfiguration));
  }

  /**
   * Get the public url, statically defined if not specified in the {@link org.obiba.agate.domain.Configuration}.
   *
   * @return
   */
  public String getPublicUrl() {
    Configuration config = getConfiguration();

    if (config.hasPublicUrl()) {
      return config.getPublicUrl();
    } else {
      String host = env.getProperty("server.address");
      String port = env.getProperty("https.port");
      return "https://" + host + ":" + port + getContextPath();
    }
  }

  public SecretKey getSecretKeyJWT() {
    Configuration config = getConfiguration();
    return asJWTSecretKey(config.getSecretKeyJWT());
  }

  /**
   * Encrypt string using secret key.
   *
   * @param plain
   * @return
   */
  public String encrypt(String plain) {
    ByteSource encrypted = cipherService.encrypt(CodecSupport.toBytes(plain), getSecretKey());
    return encrypted.toHex();
  }

  /**
   * Decrypt string using secret key.
   *
   * @param encrypted
   * @return
   */
  public String decrypt(String encrypted) {
    ByteSource decrypted;
    try {
      decrypted = cipherService.decrypt(Hex.decode(encrypted), getSecretKey());
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.warn("Falling back on legacy crypto service", e);
      }
      decrypted = legacyCipherService.decrypt(Hex.decode(encrypted), getSecretKey());
    }
    return CodecSupport.toString(decrypted.getBytes());
  }

  /**
   * Get the schema and the definition of the join form.
   *
   * @return
   * @throws JSONException
   */
  public JSONObject getJoinConfiguration(String locale, String application) throws JSONException, IOException {
    return getJoinConfiguration(locale, application, false);
  }

  public JSONObject getJoinConfiguration(String locale, String application, boolean forEditing) throws JSONException, IOException {
    Configuration config = getConfiguration();
    List<RealmConfig> realms = Strings.isNullOrEmpty(application)
      ? realmConfigService.findAllRealmsForSignup()
      : realmConfigService.findAllRealmsForSignupAndApplication(application);

    JSONObject form = UserFormBuilder.newBuilder(config, locale, applicationContext.getResource("classpath:join/formDefinition.json"))
      .realms(realms)
      .attributes(config.getUserAttributes())
      .addUsername(forEditing || config.isJoinWithUsername())
      .build();

    return new TranslationUtils().translate(form, getTranslator(locale));
  }

  /**
   * Get the schema and the definition of the profile form.
   *
   * @param locale
   * @return
   * @throws JSONException
   */
  public JSONObject getProfileConfiguration(String locale) throws JSONException, IOException {
    Configuration config = getConfiguration();
    List<RealmConfig> realms = realmConfigService.findAllRealmsForSignup();

    JSONObject form = UserFormBuilder.newBuilder(config, locale, applicationContext.getResource("classpath:join/formDefinition.json"))
      .realms(realms)
      .attributes(config.getUserAttributes())
      .build();

    return new TranslationUtils().translate(form, getTranslator(locale));
  }

  public JSONObject getRealmFormConfiguration(String locale, boolean forEditing) throws JSONException, IOException {
    TranslationUtils translationUtils = new TranslationUtils();
    Translator translator = getTranslator(locale);
    JSONObject form = new JSONObject();

    form.put(
      "form",
      translationUtils.translate(RealmConfigFormBuilder.newBuilder(forEditing).build(), translator).toString()
    );
    form.put(
      "userInfoMapping",
      translationUtils.translate(RealmUserInfoFormBuilder.newBuilder(extractUserInfoFieldsToMap(forEditing)).build(), translator).toString()
    );
    form.put(
      "userInfoMappingDefaults",
      UserInfoFieldsMappingDefaultsFactory.create()
    );
    form.put(
      AgateRealm.AGATE_LDAP_REALM.getName(),
      translationUtils.translate(LdapRealmConfigFormBuilder.newBuilder().build(), translator).toString()
    );
    form.put(
      AgateRealm.AGATE_JDBC_REALM.getName(),
      translationUtils.translate(JdbcRealmConfigFormBuilder.newBuilder().build().toString(), translator)
    );
    form.put(
      AgateRealm.AGATE_AD_REALM.getName(),
      translationUtils.translate(ActiveDirectoryRealmConfigFormBuilder.newBuilder().build().toString(), translator)
    );
    form.put(
      AgateRealm.AGATE_OIDC_REALM.getName(),
      translationUtils.translate(OidcRealmConfigFormBuilder.newBuilder().build().toString(), translator)
    );

    return form;
  }

  private List<String> extractUserInfoFieldsToMap(boolean forEditing) throws IOException, JSONException {
    List<String> exclusions = Lists.newArrayList("realm", "locale");
    JSONArray names = getJoinConfiguration("en", null, forEditing).getJSONObject("schema").getJSONObject("properties").names();
    List<String> fields = Lists.newArrayList();
    for (int i = 0; i < names.length(); i++) {
      String name = names.optString(i);
      if (!Strings.isNullOrEmpty(name) && !exclusions.contains(name.toLowerCase())) {
        fields.add(name);
      }
    }

    fields.sort(UserInfoFieldsComparator::compare);

    return fields;
  }

  public JsonNode getUserProfileTranslations(String locale) throws IOException {

    JsonNode globalTranslations = getTranslations(locale, false);
    JsonNode userProfileTranslations = globalTranslations.get("user");

    LocalizedString translations = getConfiguration().getTranslations();

    if (translations != null) {
      String customTranslations = translations.get(locale);
      if (customTranslations != null) {
        JsonNode customTranslationsParsed = objectMapper.readTree(customTranslations);
        return mergeJson(userProfileTranslations, customTranslationsParsed);
      }
    }

    return userProfileTranslations;
  }

  //
  // Private methods
  //

  private String generateSecretKey() {
    Key key = cipherService.generateNewKey();
    return Hex.encodeToString(key.getEncoded());
  }

  private String generateJWTSecretKeyString() {
    SecretKey key = Jwts.SIG.HS256.key().build(); //or HS384.key() or HS512.key()
    return Encoders.BASE64.encode(key.getEncoded());
  }

  private SecretKey asJWTSecretKey(String key) {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
  }

  private Configuration getOrCreateConfiguration() {
    if (agateConfigRepository.count() == 0) {
      Configuration configuration = new Configuration();
      configuration.getLocales().add(Configuration.DEFAULT_LOCALE);
      configuration.setSecretKey(generateSecretKey());
      configuration.setSecretKeyJWT(generateJWTSecretKeyString());
      agateConfigRepository.save(configuration);
      return getConfiguration();
    }
    Configuration configuration = agateConfigRepository.findAll().get(0);
    if (configuration.getSecretKeyJWT() == null) {
      configuration.setSecretKeyJWT(generateJWTSecretKeyString());
      agateConfigRepository.save(configuration);
    }
    return configuration;
  }

  private byte[] getSecretKey() {
    return Hex.decode(getOrCreateConfiguration().getSecretKey());
  }

  private Translator getTranslator(String locale) {
    Translator translator = JsonTranslator.buildSafeTranslator(() -> getTranslations(locale, false).toString());
    translator = new PrefixedValueTranslator(translator);
    return translator;
  }

  private DocumentContext getTranslationDocument(String locale) throws IOException {
    return JsonPath.using(conf).parse(getTranslations(locale, false).toString());
  }

  public JsonNode getTranslations(String locale, boolean _default) throws IOException {

    locale = getAvailableLocale(locale);

    JsonNode original = getFileBasedTranslations(locale);

    Configuration configuration = getOrCreateConfiguration();
    if (!_default && configuration.hasTranslations()) {
      JsonNode custom = objectMapper.readTree(configuration.getTranslations().get(locale));
      return mergeJson(original, custom);
    }

    return original;
  }

  private JsonNode getFileBasedTranslations(String locale) throws IOException {
    File translations = getTranslationsResource(locale).getFile();
    return objectMapper.readTree(translations);
  }

  private String getAvailableLocale(String locale) {
    return getTranslationsResource(locale).exists() ? locale : "en";
  }

  private JsonNode mergeJson(JsonNode mainNode, JsonNode updateNode) {
    Iterator<String> fieldNames = updateNode.fieldNames();
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      JsonNode jsonNode = mainNode.get(fieldName);
      if (jsonNode != null && jsonNode.isObject()) {
        mergeJson(jsonNode, updateNode.get(fieldName));
      } else {
        if (mainNode instanceof ObjectNode) {
          JsonNode value = updateNode.get(fieldName);
          ((ObjectNode) mainNode).replace(fieldName, value);
        }
      }
    }

    return mainNode;
  }

  private Resource getTranslationsResource(String locale) {
    return applicationContext.getResource(String.format("classpath:/i18n/%s.json", locale));
  }

  /**
   * Update translations based on additions and removals of locales.
   *
   * @param savedConfig
   * @param updatedConfig
   */
  private void updateTranslations(Configuration savedConfig, Configuration updatedConfig) {
    LocalizedString translations = updatedConfig.getTranslations();
    if (translations == null) return;

    HashSet<Locale> saved = new HashSet<>(savedConfig.getLocales());
    HashSet<Locale> updated = new HashSet<>(updatedConfig.getLocales());

    Set<Locale> addedLocales = new HashSet<>(updated);
    addedLocales.removeAll(saved);
    Set<Locale> removedLocales = new HashSet<>(saved);
    removedLocales.removeAll(updated);
    Set<Locale> commonLocales = new HashSet<>(saved);
    commonLocales.retainAll(updated);

    if (!removedLocales.isEmpty()) {
      updatedConfig.getTranslations().keySet().removeIf(locale -> removedLocales.contains(Locale.forLanguageTag(locale)));
    }

    if (!addedLocales.isEmpty()) {
      Locale defaultLocale = commonLocales.stream()
        .filter(locale -> locale.getLanguage().equalsIgnoreCase("en"))
        .findFirst()
        .orElse(commonLocales.isEmpty() ? null : commonLocales.iterator().next());

      for (Locale locale : addedLocales) {
        if (defaultLocale != null) {
          translations.put(locale.toLanguageTag(), updatedConfig.getTranslations().get(defaultLocale.toLanguageTag()));
        } else {
          translations.put(locale.toLanguageTag(), "{}");
        }
      }
    }
  }

  /**
   * Apply settings that are modified only internally.
   *
   * @param updatedConfig
   * @return
   */
  public Configuration applyInternalSettings(Configuration updatedConfig) {
    Configuration savedConfig = getConfiguration();
    updatedConfig.setSecretOtp(savedConfig.getSecretOtp());
    updatedConfig.setGroupsSeeded(savedConfig.isGroupsSeeded());
    updatedConfig.setApplicationsSeeded(savedConfig.isApplicationsSeeded());
    updateTranslations(savedConfig, updatedConfig);
    return updatedConfig;
  }
}
