package org.obiba.agate.service;

import java.security.Key;

import javax.inject.Inject;
import javax.validation.Valid;

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
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

@Component
public class ConfigurationService {

  @Inject
  private AgateConfigRepository agateConfigRepository;

  @Inject
  private EventBus eventBus;

  @Inject
  private Environment env;

  private final AesCipherService cipherService = new AesCipherService();

  @Cacheable(value = "agateConfig", key = "#root.methodName")
  public Configuration getConfiguration() {
    return getOrCreateConfiguration();
  }

  @CacheEvict(value = "agateConfig", allEntries = true)
  public void save(@Valid Configuration configuration) {
    Configuration savedConfiguration = getOrCreateConfiguration();
    BeanUtils
      .copyProperties(configuration, savedConfiguration, "id", "version", "createdBy", "createdDate", "lastModifiedBy",
        "lastModifiedDate", "secretKey");
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
  public JSONObject getJoinConfiguration() throws JSONException {
    Configuration config = getConfiguration();
    JSONObject rval = new JSONObject();
    rval.put("schema", getJoinSchema(config));
    rval.put("definition", getJoinDefinition(config));
    return rval;
  }

  //
  // Private methods
  //

  private JSONObject getJoinSchema(Configuration config) throws JSONException {
    JSONObject schema = new JSONObject();
    schema.putOnce("type", "object");
    JSONObject properties = new JSONObject();
    properties.put("email", newSchemaProperty("string", "Email") //
      .put("pattern", "^\\S+@\\S+$") //
      .put("validationMessage", "Not a valid email.") //
    );
    properties.put("username", newSchemaProperty("string", "User Name").put("minLength", 3));
    properties.put("firstname", newSchemaProperty("string", "First Name"));
    properties.put("lastname", newSchemaProperty("string", "Last Name"));

    JSONArray required = new JSONArray(Lists.newArrayList("username", "email", "firstname", "lastname"));

    if(config.hasUserAttributes()) {
      config.getUserAttributes().forEach(a -> {
        try {
          String type = a.getType().name().toLowerCase();
          JSONObject property = newSchemaProperty(type, a.getName());
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

  private JSONArray getJoinDefinition(Configuration config) throws JSONException {
    JSONArray definition = new JSONArray();
    definition.put("username");
    definition.put("email");
    definition.put("firstname");
    definition.put("lastname");
    if(config.hasUserAttributes()) {
      config.getUserAttributes().forEach(a -> {
        try {
          definition.put(newDefinitionProperty(a.getName(), a.getDescription()));
        } catch(JSONException e) {
          // ignore
        }
      });
    }
    return definition;
  }

  private JSONObject newSchemaProperty(String type, String title) throws JSONException {
    JSONObject property = new JSONObject();
    property.put("type", type);
    property.put("title", title);
    return property;
  }

  private JSONObject newDefinitionProperty(String key, String description) throws JSONException {
    JSONObject property = new JSONObject();
    property.put("key", key);
    if (!Strings.isNullOrEmpty(description)) {
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
      configuration.setSecretKey(generateSecretKey());
      agateConfigRepository.save(configuration);
      return getConfiguration();
    }
    return agateConfigRepository.findAll().get(0);
  }

  private byte[] getSecretKey() {
    return Hex.decode(getOrCreateConfiguration().getSecretKey());
  }
}
