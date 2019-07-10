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


  private RealmConfigFormBuilder(boolean forEditing) {
    List<String> realmTypes = Stream.of(AgateRealm.values())
      .filter(realm -> realm != AgateRealm.AGATE_USER_REALM && realm != AgateRealm.AGATE_TOKEN_REALM)
      .map(AgateRealm::getName)
      .collect(Collectors.toList());

    formSchema = "{" +
      "    \"type\": \"object\"," +
      "    \"properties\": {" +
      "      \"name\": {" +
      "        \"type\": \"string\"," +
      "        \"pattern\": \"^[0-9A-Za-z-_]+$\"," +
      "        \"title\": \"t(global.name)\"," +
      "        \"description\": \"t(realm.name-help)\"," +
      "        \"readonly\": " + forEditing +
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
      "        \"default\": \"" + RealmStatus.INACTIVE + "\"," +
      "        \"description\": t(realm.status-help)," +
      "        \"enum\": " + getStatusEnum() +
      "      }," +
      "      \"groups\": {" +
      "        \"title\": \"t(global.groups)\"," +
      "        \"type\": \"array\"," +
      "        \"description\": \"t(realm.groups-help)\"," +
      "        \"format\": \"obibaUiSelect\"" +
      "      }," +
      "      \"type\": {" +
      "        \"type\": \"string\"," +
      "        \"title\": \"t(global.type)\"," +
      "        \"default\": \"" + AgateRealm.AGATE_USER_REALM.getName() + "\"," +
      "        \"enum\": " + getTypeEnum(realmTypes) +
      "      }," +
      "      \"forSignup\": {" +
      "        \"type\": \"boolean\"," +
      "        \"title\": \"t(realm.for-signup)\"," +
      "        \"description\": t(realm.for-signup-help)" +
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

  public static RealmConfigFormBuilder newBuilder(boolean forEditing) {
    return new RealmConfigFormBuilder(forEditing);
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
    List<String> statusList = Stream.of(RealmStatus.values()).map(RealmStatus::toString).collect(Collectors.toList());
    return new JSONArray(statusList).toString();
  }
}
