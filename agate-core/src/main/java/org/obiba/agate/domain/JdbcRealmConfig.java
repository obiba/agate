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

public class JdbcRealmConfig {

  private static final String URL_FIELD = "url";
  private static final String AUTHENTICATION_QUERY = "authenticationQuery";
  private static final String USERNAME_FIELD = "username";
  private static final String PASSWORD_FIELD = "password";

  private String url;

  private String authenticationQuery;

  private String username;

  private String password;

  private JdbcRealmConfig() { }

  public String getUrl() {
    return url;
  }

  public String getAuthenticationQuery() {
    return authenticationQuery;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public JSONObject getAsSecuredJSONObject() throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(URL_FIELD, url);
    jsonObject.put(AUTHENTICATION_QUERY, authenticationQuery);
    jsonObject.put(USERNAME_FIELD, username);
    jsonObject.put(PASSWORD_FIELD, "");

    return jsonObject;
  }

  public static Builder newBuilder(String content) throws JSONException {
    return newBuilder(new JSONObject(content));
  }

  public static Builder newBuilder(JSONObject content) {
    return new Builder(content);
  }

  public static class Builder {

    private final JdbcRealmConfig config;

    private Builder(JSONObject content) {
      config = new JdbcRealmConfig();
      config.url = content.optString(URL_FIELD);
      config.authenticationQuery = content.optString(AUTHENTICATION_QUERY);
      config.username = content.optString(USERNAME_FIELD);
      config.password = content.optString(PASSWORD_FIELD);
    }

    public JdbcRealmConfig build() {
      return config;
    }

  }
}
