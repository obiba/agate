/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.validation.Valid;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.util.ByteSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.event.AgateConfigUpdatedEvent;
import org.obiba.agate.repository.AgateConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;

import static com.jayway.jsonpath.Configuration.defaultConfiguration;

@Component
public class ConfigurationService {

  private static final Logger log = LoggerFactory.getLogger(ConfigurationService.class);

  private static final com.jayway.jsonpath.Configuration conf = defaultConfiguration();

  @Inject
  private AgateConfigRepository agateConfigRepository;

  @Inject
  private EventBus eventBus;

  @Inject
  private Environment env;

  @Inject
  private ApplicationContext applicationContext;

  @Inject
  private ObjectMapper objectMapper;

  private final AesCipherService cipherService = new AesCipherService();

  @Cacheable(value = "agateConfig", key = "#root.methodName")
  public Configuration getConfiguration() {
    Configuration configuration = getOrCreateConfiguration();
    if (configuration.getLocales().size() == 0) configuration.getLocales().add(Configuration.DEFAULT_LOCALE);
    return configuration;
  }

  @CacheEvict(value = "agateConfig", allEntries = true)
  public void save(@Valid Configuration configuration) {
    Configuration savedConfiguration = getOrCreateConfiguration();
    BeanUtils
      .copyProperties(configuration, savedConfiguration, "id", "version", "createdBy", "createdDate", "lastModifiedBy",
        "lastModifiedDate", "secretKey", "agateVersion");
    if(configuration.getAgateVersion() != null) savedConfiguration.setAgateVersion(configuration.getAgateVersion());
    agateConfigRepository.save(savedConfiguration);
    eventBus.post(new AgateConfigUpdatedEvent(getConfiguration()));
  }

  /**
   * Get the public url, statically defined if not specified in the {@link org.obiba.agate.domain.Configuration}.
   *
   * @return
   */
  public String getPublicUrl() {
    Configuration config = getConfiguration();

    if(config.hasPublicUrl()) {
      return config.getPublicUrl();
    } else {
      String host = env.getProperty("server.address");
      String port = env.getProperty("https.port");
      return "https://" + host + ":" + port;
    }
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
    ByteSource decrypted = cipherService.decrypt(Hex.decode(encrypted), getSecretKey());
    return CodecSupport.toString(decrypted.getBytes());
  }

  /**
   * Get the schema and the definition of the join form.
   *
   * @return
   * @throws JSONException
   */
  public JSONObject getJoinConfiguration(String locale) throws JSONException, IOException {
    Configuration config = getConfiguration();
    JSONObject rval = new JSONObject();
    rval.put("schema", getJoinSchema(config));
    rval.put("definition", getJoinDefinition(config));
    translate(rval, getTranslationDocument(locale));
    return rval;
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
    JSONObject rval = new JSONObject();
    rval.put("schema", getProfileSchema(config));
    rval.put("definition", getProfileDefinition(config));
    translate(rval, getTranslationDocument(locale));
    return rval;
  }

  public JsonNode getUserRelatedTranslations(String locale) throws IOException {

    String customTranslations = getConfiguration().getTranslations().get(locale);
    JsonNode globalTranslations = getTranslations(locale, false);
    JsonNode user = globalTranslations.get("user");

    JsonNode customTranslations2 = objectMapper.readTree(customTranslations);

    return mergeJson(user, customTranslations2);
  }

  //
  // Private methods
  //

  private JSONObject getJoinSchema(Configuration config) throws JSONException, IOException {
    return getUserFormSchema(config, config.isJoinWithUsername());
  }

  private JSONArray getJoinDefinition(Configuration config) throws JSONException, IOException {
    return getUserFormDefinition(config, applicationContext.getResource("classpath:join/formDefinition.json"),
      config.isJoinWithUsername());
  }

  private JSONObject getProfileSchema(Configuration config) throws JSONException, IOException {
    return getUserFormSchema(config, false);
  }

  private JSONArray getProfileDefinition(Configuration config) throws JSONException, IOException {
    return getUserFormDefinition(config, applicationContext.getResource("classpath:profile/formDefinition.json"), false);
  }

  private JSONObject getUserFormSchema(Configuration config, boolean withUsername) throws JSONException, IOException {
    JSONObject schema = new JSONObject();
    schema.putOnce("type", "object");
    JSONObject properties = new JSONObject();
    properties.put("email", newSchemaProperty("string", "t(user.email)") //
      .put("pattern", "^\\S+@\\S+$") //
      .put("validationMessage", "t(user.email-invalid)") //
    );
    JSONArray required = new JSONArray();
    if(withUsername) {
      properties.put("username", newSchemaProperty("string", "t(user.name)").put("minLength", 3));
      required.put("username");
    }
    properties.put("firstname", newSchemaProperty("string", "t(user.firstName)"));
    properties.put("lastname", newSchemaProperty("string", "t(user.lastName)"));

    properties.put("locale", newSchemaProperty("string", "t(user.preferredLanguage)")
      .put("enum", config.getLocalesAsString()).put("default", Configuration.DEFAULT_LOCALE.getLanguage()));

    Lists.newArrayList("email", "firstname", "lastname", "locale").forEach(required::put);

    if(config.hasUserAttributes()) {
      config.getUserAttributes().forEach(a -> {
        try {
          String type = a.getType().name().toLowerCase();
          JSONObject property = newSchemaProperty(type, "t(" + a.getName() + ")");
          if(a.hasValues()) {
            //noinspection ConstantConditions
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

  private JSONArray getUserFormDefinition(Configuration config, Resource formDefinitionResource, boolean withUsername)
    throws JSONException, IOException {
    if(formDefinitionResource != null && formDefinitionResource.exists()) {
      JSONArray def = new JSONArray(IOUtils.toString(formDefinitionResource.getInputStream()));

      if(!withUsername) {
        // look for username and remove it
        // note that only works with a simple schema form definition
        JSONArray ndef = new JSONArray();
        for(int i = 0; i < def.length(); i++) {
          Object obj = def.get(i);
          if(!(obj instanceof JSONObject) || !((JSONObject) obj).has("key") ||
            !"username".equals(((JSONObject) obj).get("key"))) {
            ndef.put(obj);
          }
        }
        def = ndef;
      }

      return def;
    }

    JSONArray definition = new JSONArray();

    if(withUsername) {
      definition.put(newDefinitionProperty("username","t(user.name)", ""));
    }

    definition.put(newDefinitionProperty("email","t(user.email)", ""));
    definition.put(newDefinitionProperty("firstname","t(user.firstName)", ""));
    definition.put(newDefinitionProperty("lastname","t(user.lastName)", ""));

    JSONObject localeTitleMap = new JSONObject();
    config.getLocalesAsString().forEach(l -> {
      try {
        localeTitleMap.put(l, "t(language." + l + ")");
      } catch (JSONException e) {
        // ignored
      }
    });

    definition.put(newDefinitionProperty("locale", "t(user.preferredLanguage)", "")
      .put("titleMap", localeTitleMap));

    if(config.hasUserAttributes()) {
      config.getUserAttributes().forEach(a -> {
        try {
          JSONObject property = newDefinitionProperty(a.getName(), "t(" + a.getName() + ")", a.hasDescription() ? "t(" + a.getDescription() + ")" : "");
          if(a.hasValues()) {
            JSONObject titleMap = new JSONObject();
            //noinspection ConstantConditions
            a.getValues().forEach(e -> {
              try {
                titleMap.put(e, "t(" + e + ")");
              } catch(JSONException e1) {
                // ignored
              }
            });
            property.put("titleMap", titleMap);
          }
          definition.put(property);
        } catch(JSONException e) {
          // ignore
        }
      });
    }

    return definition;
  }

  private void translate(JSONObject object, DocumentContext translations) throws JSONException {
    Iterator<String> keys = object.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      Object property = object.get(key);
      log.debug("{} : {}", key, property.toString());
      if (property instanceof String) {
        object.put(key, translate((String)property, translations));
      } else if (property instanceof JSONArray) {
        translate((JSONArray) property, translations);
      } else if (property instanceof JSONObject) {
        translate((JSONObject) property, translations);
      }
    }
  }

  private void translate(JSONArray array, DocumentContext translations) throws JSONException {
    for (int i = 0; i<array.length(); i++) {
      Object value = array.get(i);
      if (value instanceof JSONArray) {
        translate((JSONArray) value, translations);
      } else if (value instanceof JSONObject) {
        translate((JSONObject) value, translations);
      } else if (value instanceof String) {
        array.put(i, translate((String)value, translations));
      }
    }
  }

  private String translate(String value, DocumentContext translations) {
    if (Strings.isNullOrEmpty(value)) return value;
    Pattern p = Pattern.compile("t\\(([^\\)]+)\\)");
    Matcher m = p.matcher(value);
    StringBuffer s = new StringBuffer();
    while (m.find()) {
      String key = m.group(1);
      try {
        String translated = translations.read("$." + key);
        m.appendReplacement(s, translated);
      } catch(JsonPathException e) {
        m.appendReplacement(s, key);
      }
    }
    return s.length() == 0 ? value : s.toString();
  }

  private JSONObject newSchemaProperty(String type, String title) throws JSONException {
    JSONObject property = new JSONObject();
    property.put("type", type);
    property.put("title", title);
    return property;
  }

  private JSONObject newDefinitionProperty(String key, String title, String description) throws JSONException {
    JSONObject property = new JSONObject();
    property.put("key", key);
    if(!Strings.isNullOrEmpty(title)) {
      property.put("title", title);
    }
    if(!Strings.isNullOrEmpty(description)) {
      property.put("description", description);
    }
    return property;
  }

  private String generateSecretKey() {
    Key key = cipherService.generateNewKey();
    return Hex.encodeToString(key.getEncoded());
  }

  private Configuration getOrCreateConfiguration() {
    if(agateConfigRepository.count() == 0) {
      Configuration configuration = new Configuration();
      configuration.getLocales().add(Configuration.DEFAULT_LOCALE);
      configuration.setSecretKey(generateSecretKey());
      agateConfigRepository.save(configuration);
      return getConfiguration();
    }
    return agateConfigRepository.findAll().get(0);
  }

  private byte[] getSecretKey() {
    return Hex.decode(getOrCreateConfiguration().getSecretKey());
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
      }
      else {
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
}
