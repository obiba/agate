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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class RealmUserInfoFormBuilder extends BaseRealmConfigFormBuilder {

  private RealmUserInfoFormBuilder(List<String> userInfoFields) {

    formSchema = "{" +
      "  \"type\": \"object\"," +
      "  \"properties\": {" +
      "    \"userInfoMapping\": {" +
      "      \"type\": \"object\"," +
      "      \"properties\": {" +
      "         \"type\": \"object\"," +
                addFieldSchema(userInfoFields) +
      "      }" +
      "    }" +
      "  }" +
      "}";

    formDefinition = "[" +
      "  {" +
      "    \"notitle\": " + true + "," +
      "    \"key\": \"userInfoMapping\"," +
      "    \"items\": " + addFieldDefinitions(userInfoFields) +
      "  }" +
      "]";
  }

  public static RealmUserInfoFormBuilder newBuilder(List<String> userInfoFields) {
    return new RealmUserInfoFormBuilder(userInfoFields);
  }

  private String addFieldSchema(Collection<String> userInfoFields) {
    return userInfoFields
      .stream()
      .map(field -> String.format("\"%s\": {\"title\": \"t(user-info.%s)\", \"type\": \"string\"}", field, field))
      .collect(Collectors.joining(","));
  }

  private String addFieldDefinitions(Iterable<String> userInfoFields) {
    JSONArray fields = new JSONArray();
    userInfoFields.forEach(field -> {
      if ("groupsJS".equals(field)) {
        JSONObject obj = new JSONObject();
        try {
          obj.put("key", "userInfoMapping." + field);
          obj.put("type", "textarea");
          obj.put("title", String.format("t(user-info.%s)", field));
          obj.put("description", String.format("t(user-info.%s-help)", field));
          obj.put("placeholder", "// input: userInfo\n" +
              "// output: string or array of strings\n\n" +
              "// example:\n" +
              "// userInfo.some.property.map(x => x.split(':')[0])");
          fields.put(obj);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      } else {
        JSONObject obj = new JSONObject();
        try {
          obj.put("key", "userInfoMapping." + field);
          obj.put("type", "string");
          obj.put("title", String.format("t(user-info.%s)", field));
          obj.put("description", String.format("t(user-info.%s-help)", field));
          fields.put(obj);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    });
    return fields.toString();
  }
}
