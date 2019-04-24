package org.obiba.agate.service.support;

import com.google.common.collect.Lists;
import io.jsonwebtoken.lang.Assert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.RealmStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class RealmConfigForm {

  private static final Logger log = LoggerFactory.getLogger(RealmConfigForm.class);

  private static String FORM_SCHEMA;

  private static String FORM_DEFINITION;

  private static JSONObject FORM;

  static {
    try {
      FORM_SCHEMA = "{" +
        "    \"type\": \"object\"," +
        "    \"properties\": {" +
        "      \"name\": {" +
        "        \"type\": \"string\"," +
        "        \"title\": \"t(realm.name)\"" +
        "      }," +
        "      \"title\": {" +
        "        \"type\": \"string\"," +
        "        \"title\": \"t(realm.title)\"" +
        "      }," +
        "      \"description\": {" +
        "        \"type\": \"string\"," +
        "        \"title\": \"t(realm.description)\"" +
        "      }," +
        "      \"status\": {" +
        "        \"type\": \"string\"," +
        "        \"title\": \"t(realm.status)\"," +
        "        \"enum\": " + getStatusEnum() +
        "      }," +
        "      \"groups\": {" +
        "        \"type\": \"array\"," +
        "        \"title\": \"t(realm.groups)\"," +
        "        \"items\": {" +
        "          \"type\": \"string\"" +
        "        }" +
        "      }," +
        "      \"type\": {" +
        "        \"type\": \"string\"," +
        "        \"title\": \"t(realm.type)\"," +
        "        \"enum\": " + getTypeEnum() +
        "      }," +
        "      \"defaultRealm\": {" +
        "        \"type\": \"boolean\"," +
        "        \"title\": \"t(realm.default-realm)\"" +
        "      }," +
        "      \"forSignup\": {" +
        "        \"type\": \"boolean\"," +
        "        \"title\": \"t(realm.for-signup)\"" +
        "      }" +
        "    }," +
        "    \"required\": [" +
        "      \"name\"," +
        "      \"type\"" +
        "    ]" +
        "  }";

      FORM_DEFINITION = "[" +
        "  \t{" +
        "      \"type\": \"section\"," +
        "      \"items\": [" +
        "        {" +
        "          \"key\": \"name\"" +
        "        }," +
        "        {" +
        "          \"type\": \"help\"," +
        "          \"helpvalue\": \"<h4>t(realm.name-help)</h4>\"" +
        "        }" +
        "      ]" +
        "    }," +
        "    \"title\"," +
        "    \"description\"," +
        "    \"status\"," +
        "    {" +
        "      \"type\": \"section\"," +
        "      \"items\": [" +
        "        {" +
        "          \"key\": \"groups\"" +
        "        }," +
        "        {" +
        "          \"type\": \"help\"," +
        "          \"helpvalue\": \"<h4>t(realm.groups-help)</h4>\"" +
        "        }" +
        "      ]" +
        "    }," +
        "    {" +
        "      \"type\": \"section\"," +
        "      \"items\": [" +
        "        {" +
        "          \"key\": \"type\"" +
        "        }," +
        "        {" +
        "          \"type\": \"help\"," +
        "          \"helpvalue\": \"<h4>t(realm.type-help)</h4>\"" +
        "        }" +
        "      ]" +
        "    }," +
        "    {" +
        "      \"type\": \"section\"," +
        "      \"items\": [" +
        "        {" +
        "          \"key\": \"defaultRealm\"" +
        "        }," +
        "        {" +
        "          \"type\": \"help\"," +
        "          \"helpvalue\": \"<h4>t(realm.default-realm-help)</h4>\"" +
        "        }" +
        "      ]" +
        "    }," +
        "    {" +
        "      \"type\": \"section\"," +
        "      \"items\": [" +
        "        {" +
        "          \"key\": \"forSignup\"" +
        "        }," +
        "        {" +
        "          \"type\": \"help\"," +
        "          \"helpvalue\": \"<h4>t(realm.for-signup-help)</h4>\"" +
        "        }" +
        "      ]" +
        "    }" +
        "  ]";

      FORM = new JSONObject();
      FORM.put("schema", new JSONObject(FORM_SCHEMA));
      FORM.put("definition", new JSONArray(FORM_DEFINITION));

    } catch (JSONException e) {
      log.error("Invalid JSON format {}", e);
    }
  }

  private static String getTypeEnum() {
    ArrayList<String> realmTypes = Lists.newArrayList(AgateRealm.AGATE_LDAP_REALM.toString());
    return new JSONArray(realmTypes).toString();
  }

  private static String getStatusEnum() throws JSONException {
    RealmStatus[] values = RealmStatus.values();
    return new JSONArray(values).toString();
  }

  public static JSONObject getForm() {
    Assert.notNull(FORM, "Realm form cannot be null.");
    return FORM;
  }
}
