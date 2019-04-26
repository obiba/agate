package org.obiba.agate.service.support;

import org.json.JSONArray;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.domain.RealmStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class RealmConfigFormBuilder extends BaseRealmConfigFormBuilder {


  private RealmConfigFormBuilder(RealmConfig defaultRealm) {
    List<String> realmTypes = Stream.of(AgateRealm.values())
      .filter(realm -> realm != AgateRealm.AGATE_USER_REALM && realm != AgateRealm.AGATE_TOKEN_REALM)
      .map(AgateRealm::getName)
      .collect(Collectors.toList());

    formSchema = "{" +
      "    \"type\": \"object\"," +
      "    \"properties\": {" +
      "      \"name\": {" +
      "        \"type\": \"string\"," +
      "        \"pattern\": \"[0-9A-Za-z-_\\s]\"," +
      "        \"title\": \"t(global.name)\"," +
      "        \"description\": \"t(realm.name-help)\"" +
      "      }," +
      "      \"title\": {" +
      "        \"type\": \"string\"," +
      "        \"title\": \"t(global.title)\"" +
      "      }," +
      "      \"description\": {" +
      "        \"type\": \"string\"," +
      "        \"title\": \"t(global.description)\"" +
      "      }," +
      "      \"status\": {" +
      "        \"type\": \"string\"," +
      "        \"title\": \"t(global.status)\"," +
      "        \"enum\": " + getStatusEnum() +
      "      }," +
      "      \"groups\": {" +
      "        \"title\": \"t(global.groups)\"," +
      "        \"type\": \"array\"," +
      "        \"items\": {" +
      "          \"type\": \"string\"," +
      "          \"title\": \" \"" +
      "        }" +
      "      }," +
      "      \"type\": {" +
      "        \"type\": \"string\"," +
      "        \"title\": \"t(global.type)\"," +
      "        \"default\":" + getDefaultType(defaultRealm, realmTypes) + "," +
      "        \"enum\": " + getTypeEnum(realmTypes) +
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

    formDefinition = "[" +
      "  {" +
      "      \"type\": \"section\"," +
      "      \"items\": [" +
      "        \"name\"" +
      "      ]" +
      "    }," +
      "    \"title\"," +
      "    \"description\"," +
      "    \"status\"," +
      "    {" +
      "      \"key\": \"type\"," +
      "      \"type\": \"select\"," +
      "      \"titleMap\": " + getTypeTitleMap(realmTypes) +
      "    }," +
      "    \"defaultRealm\"," +
      "    \"forSignup\"," +
      "    {" +
      "      \"key\": \"groups\"," +
      "      \"add\": \"t(group.add)\"," +
      "      \"style\": {" +
      "        \"add\" : \"btn btn-info btn-sm btn-plus\"" +
      "      }" +
      "    }" +
      "  ]";
  }

  private String getDefaultType(RealmConfig defaultRealm, List<String> realmTypes) {
    return defaultRealm == null ? realmTypes.get(0) : defaultRealm.getType().getName();
  }

  public static RealmConfigFormBuilder newBuilder(RealmConfig defaultRealm) {
    return new RealmConfigFormBuilder(defaultRealm);
  }

  private String getTypeEnum(List<String> realmTypes) {
    return new JSONArray(realmTypes).toString();
  }

  private String getTypeTitleMap(List<String> realmTypes) {
    List<Map<String, String>> realms =
      realmTypes.stream().map(type ->
        new HashMap<String, String>() {
        {
          put("value", type);
          put("name", String.format("t(realm.%s)", type));
        }
      })
      .collect(Collectors.toList());

    return new JSONArray(realms).toString();
  }

  private String getStatusEnum() {
    List<String> stautsList = Stream.of(RealmStatus.values()).map(RealmStatus::toString).collect(Collectors.toList());
    return new JSONArray(stautsList).toString();
  }
}
