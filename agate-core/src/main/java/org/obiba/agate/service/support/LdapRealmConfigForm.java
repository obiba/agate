package org.obiba.agate.service.support;

import io.jsonwebtoken.lang.Assert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapRealmConfigForm {

  private static final Logger log = LoggerFactory.getLogger(LdapRealmConfigForm.class);

  private static String FORM_SCHEMA;

  private static String FORM_DEFINITION;

  private static JSONObject FORM;

  static {
    try {
      FORM_SCHEMA = "{" +
        "  \"type\": \"object\"," +
        "  \"properties\": {" +
        "    \"url\": {" +
        "      \"type\": \"string\"," +
        "      \"title\": \"t(ldap-realm.url)\"" +
        "    }," +
        "    \"systemUser\": {" +
        "      \"type\": \"string\"," +
        "      \"title\": \"t(ldap-realm.system-user)\"" +
        "    }," +
        "    \"systemPassword\": {" +
        "      \"type\": \"string\"," +
        "      \"title\": \"t(ldap-realm.system-password)\"," +
        "      \"format\": \"password\"" +
        "    }," +
        "    \"userDnTemplate\": {" +
        "      \"type\": \"string\"," +
        "      \"title\": \"t(ldap-realm.user-dn-template)\"" +
        "    }" +
        "  }," +
        "  \"required\": [" +
        "    \"url\"," +
        "    \"systemUser\"," +
        "    \"systemPassword\"," +
        "    \"userDnTemplate\"" +
        "  ]" +
        "}";

      FORM_DEFINITION = "[" +
        "{" +
        "  \"type\": \"section\"," +
        "  \"items\": [" +
        "      \"url\"," +
        "      \"systemUser\"," +
        "      {" +
        "        \"key\": \"systemPassword\"," +
        "        \"type\": \"password\"" +
        "      }," +
        "      {" +
        "        \"type\": \"section\"," +
        "        \"items\": [" +
        "          {" +
        "            \"key\": \"userDnTemplate\"," +
        "            \"placeholder\": \"uid={0},ou=People,dc=example,dc=com\"" +
        "          }," +
        "          {" +
        "            \"type\": \"help\"," +
        "            \"helpvalue\": \"<h4>t(ldap-realm.user-dn-template)</h4>\"" +
        "          }" +
        "        ]" +
        "      }" +
        "    ]" +
        "  }" +
        "]";

      FORM = new JSONObject();
      FORM.put("schema", new JSONObject(FORM_SCHEMA));
      FORM.put("definition", new JSONArray(FORM_DEFINITION));

    } catch (JSONException e) {
      log.error("Invalid JSON format {}", e);
    }
  }

  public static JSONObject getForm() {
    Assert.notNull(FORM, "Ldap realm form cannot be null.");
    return FORM;
  }

}
