/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service.support;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.AttributeConfiguration;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.LocalizedString;
import org.obiba.agate.domain.RealmConfig;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class UserFormBuilder {

  private final String locale;

  private final Configuration config;

  private final Resource formDefinitionResource;

  private List<RealmConfig> realms = new LinkedList<>();

  private List<AttributeConfiguration> attributes = Lists.newArrayList();

  private boolean withUsername = false;

  private UserFormBuilder(Configuration config, String locale, Resource formDefinitionResource) {
    this.config = config;
    this.locale = locale;
    this.formDefinitionResource = formDefinitionResource;
  }

  public static UserFormBuilder newBuilder(Configuration config, String locale, Resource formDefinitionResource) {
    return new UserFormBuilder(config, locale, formDefinitionResource);
  }

  public JSONObject build() throws IOException, JSONException {
    JSONObject form = new JSONObject();
    form.put("schema", getUserFormSchema());
    form.put("definition", getUserFormDefinition());

    return form;
  }

  public UserFormBuilder realms(List<RealmConfig> realms) {
    if (realms != null) this.realms = realms;
    return this;
  }

  public UserFormBuilder attributes(List<AttributeConfiguration> attributes) {
    if (attributes != null) this.attributes = attributes;
    return this;
  }

  public UserFormBuilder addUsername(boolean value) {
    withUsername = value;
    return this;
  }

  private JSONObject getUserFormSchema() throws JSONException {
    JSONObject schema = new JSONObject();
    schema.putOnce("type", "object");
    JSONObject properties = new JSONObject();
    properties.put("email", newSchemaProperty("string", "t(user-info.email)") //
      .put("pattern", "^\\S+@\\S+$") //
      .put("validationMessage", "t(user-info.email-invalid)") //
    );
    JSONArray required = new JSONArray();
    if (withUsername) {
      properties.put("username", newSchemaProperty("string", "t(user-info.username)").put("minLength", 3));
      required.put("username");
    }

    List<String> list = Lists.newArrayList(AgateRealm.AGATE_USER_REALM.getName());
    realms.stream().map(RealmConfig::getName).forEach(list::add);

    properties.put("realm", newSchemaProperty("string", "t(user-info.realm)")
      .put("enum", list)
      .put("default", AgateRealm.AGATE_USER_REALM.getName()));
    properties.put("firstname", newSchemaProperty("string", "t(user-info.firstname)"));
    properties.put("lastname", newSchemaProperty("string", "t(user-info.lastname)"));

    properties.put("locale", newSchemaProperty("string", "t(user-info.locale)")
      .put("enum", config.getLocalesAsString()).put("default", Configuration.DEFAULT_LOCALE.getLanguage()));

    Lists.newArrayList("email", "firstname", "lastname", "locale").forEach(required::put);

    attributes.forEach(a -> {
      try {
        String type = a.getType().name().toLowerCase();
        JSONObject property = newSchemaProperty(type, "t(" + a.getName() + ")");
        if (a.hasValues()) {
          //noinspection ConstantConditions
          a.getValues().forEach(e -> {
            try {
              property.append("enum", e);
            } catch (JSONException e1) {
              // ignored
            }
          });
        }
        properties.put(a.getName(), property);
        if (a.isRequired()) required.put(a.getName());
      } catch (JSONException ignore) {
      }
    });

    schema.put("properties", properties);
    schema.put("required", required);

    return schema;
  }

  private JSONArray getUserFormDefinition()
    throws JSONException, IOException {

    if (formDefinitionResource != null && formDefinitionResource.exists()) {
      JSONArray def = new JSONArray(IOUtils.toString(formDefinitionResource.getInputStream()));

      if (!withUsername) {
        // look for username and remove it
        // note that only works with a simple schema form definition
        JSONArray ndef = new JSONArray();
        for (int i = 0; i < def.length(); i++) {
          Object obj = def.get(i);
          if (!(obj instanceof JSONObject) || !((JSONObject) obj).has("key") ||
            !"username".equals(((JSONObject) obj).get("key"))) {
            ndef.put(obj);
          }
        }
        def = ndef;
      }

      return def;
    }

    JSONArray definition = new JSONArray();

    if (withUsername) {
      definition.put(newDefinitionProperty("username", "t(user-info.username)", ""));
    }
    JSONObject realmTitleMap = new JSONObject();
    realmTitleMap.put(AgateRealm.AGATE_USER_REALM.getName(), "t(realm.default)");

    realms.stream().forEach(realmConfig -> {
      try {
        LocalizedString title = realmConfig.getTitle();
        realmTitleMap.put(realmConfig.getName(), title == null || title.get(locale) == null ? realmConfig.getName() : title.get(locale));
      } catch (JSONException e) {
        //
      }
    });

    definition.put(newDefinitionProperty("realm", "t(user-info.realm)", "").put("titleMap", realmTitleMap));
    definition.put(newDefinitionProperty("email", "t(user-info.email)", ""));
    definition.put(newDefinitionProperty("firstname", "t(user-info.firstname)", ""));
    definition.put(newDefinitionProperty("lastname", "t(user-info.lastname)", ""));

    JSONObject localeTitleMap = new JSONObject();
    config.getLocalesAsString().forEach(l -> {
      try {
        localeTitleMap.put(l, "t(language." + l + ")");
      } catch (JSONException e) {
        // ignored
      }
    });

    definition.put(newDefinitionProperty("locale", "t(user-info.locale)", "")
      .put("titleMap", localeTitleMap));

    if (config.hasUserAttributes()) {
      config.getUserAttributes().forEach(a -> {
        try {
          JSONObject property = newDefinitionProperty(a.getName(), "t(" + a.getName() + ")", a.hasDescription() ? "t(" + a.getDescription() + ")" : "");
          if (a.hasValues()) {
            JSONObject titleMap = new JSONObject();
            //noinspection ConstantConditions
            a.getValues().forEach(e -> {
              try {
                titleMap.put(e, "t(" + e + ")");
              } catch (JSONException e1) {
                // ignored
              }
            });
            property.put("titleMap", titleMap);
          }
          definition.put(property);
        } catch (JSONException e) {
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

  private JSONObject newDefinitionProperty(String key, String title, String description) throws JSONException {
    JSONObject property = new JSONObject();
    property.put("key", key);
    if (!Strings.isNullOrEmpty(title)) {
      property.put("title", title);
    }
    if (!Strings.isNullOrEmpty(description)) {
      property.put("description", description);
    }
    return property;
  }
}
