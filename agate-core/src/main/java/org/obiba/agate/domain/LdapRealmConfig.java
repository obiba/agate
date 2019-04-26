/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.domain;

import org.json.JSONException;
import org.json.JSONObject;

public class LdapRealmConfig {

  private static final String URL_FIELD = "url";
  private static final String USER_DN_TEMPLATE_FIELD = "userDnTemplate";
  private static final String SYSTEM_USERNAME_FIELD = "systemUsername";
  private static final String SYSTEM_PASSWORD_FIELD = "systemPassword";

  private String url;

  private String userDnTemplate;

  private String systemUsername;

  private String systemPassword;

  private LdapRealmConfig() { }

  public String getUrl() {
    return url;
  }

  public String getUserDnTemplate() {
    return userDnTemplate;
  }

  public String getSystemUsername() {
    return systemUsername;
  }

  public String getSystemPassword() {
    return systemPassword;
  }

  public JSONObject getAsSecuredJSONObject() throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(URL_FIELD, url);
    jsonObject.put(USER_DN_TEMPLATE_FIELD, userDnTemplate);
    jsonObject.put(SYSTEM_USERNAME_FIELD, systemUsername);
    jsonObject.put(SYSTEM_PASSWORD_FIELD, "");

    return jsonObject;
  }

  public static Builder newBuilder(String content) throws JSONException {
    return newBuilder(new JSONObject(content));
  }

  public static Builder newBuilder(JSONObject content) {
    return new Builder(content);
  }

  public static class Builder {

    private final LdapRealmConfig config;

    private Builder(JSONObject content) {
      config = new LdapRealmConfig();
      config.url = content.optString(URL_FIELD);
      config.userDnTemplate = content.optString(USER_DN_TEMPLATE_FIELD);
      config.systemUsername = content.optString(SYSTEM_USERNAME_FIELD);
      config.systemPassword = content.optString(SYSTEM_PASSWORD_FIELD);
    }

    public LdapRealmConfig build() {
      return config;
    }

  }

}
