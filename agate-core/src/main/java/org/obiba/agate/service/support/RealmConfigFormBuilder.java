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


  private RealmConfigFormBuilder(RealmConfig defaultRealm, boolean forEditing) {
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
      "        \"description\": \"t(realm.name-help)\"," +
      "        \"readonly\": \"" + forEditing + "\"" +
      "      }," +
      "      \"title\": {" +
      "        \"type\": \"object\"," +
      "        \"format\": \"localizedString\"," +
      "        \"title\": \"t(global.title)\"" +
      "      }," +
      "      \"description\": {" +
      "        \"type\": \"object\"," +
      "        \"format\": \"localizedString\"," +
      "        \"title\": \"t(global.description)\"" +
      "      }," +
      "      \"status\": {" +
      "        \"type\": \"string\"," +
      "        \"title\": \"t(global.status)\"," +
      "        \"default\": '" + RealmStatus.INACTIVE + "'," +
      "        \"enum\": " + getStatusEnum() +
      "      }," +
      "      \"groups\": {" +
      "        \"title\": \"t(global.groups)\"," +
      "        \"type\": \"array\"," +
      "        \"format\": \"obibaUiSelect\"" +
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
      "  \"name\"," +
      "  {" +
      "    \"key\": \"title\"," +
      "    \"type\": \"localizedstring\"" +
      "  }," +
      "  {" +
      "    \"key\": \"description\"," +
      "    \"type\": \"localizedstring\"," +
      "    \"rows\": 3," +
      "    \"marked\": true" +
      "  }," +
      "  {" +
      "    \"key\": \"status\"," +
      "    \"type\": \"select\"" +
      "  }," +
      "  {" +
      "    \"key\": \"type\"," +
      "    \"type\": \"select\"," +
      "    \"titleMap\": " + getTypeTitleMap(realmTypes) +
      "  }," +
      "  {" +
      "    \"key\": \"defaultRealm\"," +
      "    \"condition\": \"model.status === 'ACTIVE'\"" +
      "  }," +
      "  {" +
      "    \"key\": \"forSignup\"," +
      "    \"condition\": \"model.status === 'ACTIVE'\"" +
      "  }," +
      "  {" +
      "    \"key\": \"groups\"," +
      "    \"type\": \"obibaUiSelect\"," +
      "    \"multiple\": \"true\"" +
      "  }" +
      "]";
  }

  private String getDefaultType(RealmConfig defaultRealm, List<String> realmTypes) {
    return defaultRealm == null ? realmTypes.get(0) : defaultRealm.getType().getName();
  }

  public static RealmConfigFormBuilder newBuilder(RealmConfig defaultRealm, boolean forEditing) {
    return new RealmConfigFormBuilder(defaultRealm, forEditing);
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

  private String getStatusTitleMap(List<String> statusList) {
    List<Map<String, String>> realms =
      statusList.stream().map(status ->
        new HashMap<String, String>() {
        {
          put("value", status);
          put("name", status);
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
