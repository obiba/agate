package org.obiba.agate.service.support;

import org.json.JSONArray;
import org.obiba.agate.domain.AgateRealm;
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
      "        \"default\": \"" + realmTypes.get(0) + "\"," +
      "        \"enum\": " + getTypeEnum(realmTypes) +
      "      }," +
      "      \"forSignup\": {" +
      "        \"type\": \"boolean\"," +
      "        \"title\": \"t(realm.for-signup)\"," +
      "        \"description\": t(realm.for-signup-help)" +
      "      }," +
      "      \"usernameClaim\": {" +
      "        \"type\": \"string\"," +
      "        \"title\": \"t(realm.username-claim)\"," +
      "        \"description\": t(realm.username-claim-help)" +
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
      "    \"type\": \"select\"," +
      "    \"titleMap\": " + getTitleMap(getStatusNames()) +
      "  }," +
      "  {" +
      "    \"key\": \"type\"," +
      "    \"type\": \"select\"," +
      "    \"titleMap\": " + getTitleMap(realmTypes) +
      "  }," +
      "  {" +
      "    \"key\": \"forSignup\"," +
      "    \"condition\": \"model.status === 'ACTIVE'\"" +
      "  }," +
      "  {" +
      "    \"key\": \"groups\"," +
      "    \"type\": \"obibaUiSelect\"," +
      "    \"multiple\": \"true\"" +
      "  }," +
      "  \"usernameClaim\"" +
      "]";
  }

  public static RealmConfigFormBuilder newBuilder(boolean forEditing) {
    return new RealmConfigFormBuilder(forEditing);
  }

  private String getTypeEnum(List<String> realmTypes) {
    return new JSONArray(realmTypes).toString();
  }

  private String getTitleMap(List<String> items) {
    List<Map<String, String>> realms =
      items.stream().map(item ->
        new HashMap<String, String>() {
        {
          put("value", item);
          put("name", String.format("t(realm.%s)", item));
        }
      })
      .collect(Collectors.toList());

    return new JSONArray(realms).toString();
  }

  private List<String> getStatusNames() {
    return Stream.of(RealmStatus.values()).map(RealmStatus::name).collect(Collectors.toList());
  }

  private String getStatusEnum() {
    return new JSONArray(getStatusNames()).toString();
  }
}
