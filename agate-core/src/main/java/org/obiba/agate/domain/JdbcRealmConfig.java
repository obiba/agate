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

import org.apache.shiro.realm.jdbc.JdbcRealm.SaltStyle;
import org.json.JSONException;
import org.json.JSONObject;

public class JdbcRealmConfig {

  private static final String URL_FIELD = "url";
  private static final String AUTHENTICATION_QUERY = "authenticationQuery";
  private static final String USERNAME_FIELD = "username";
  private static final String PASSWORD_FIELD = "password";
  private static final String SALT_STYLE_FIELD = "saltStyle";
  private static final String EXTERNAL_SALT_FIELD = "externalSalt";
  private static final String ALGORITHM_NAME_FIELD = "algorithmName";

  private String url;

  private String authenticationQuery;

  private String username;

  private String password;

  private SaltStyle saltStyle;

  private String externalSalt;

  private String algorithmName;

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

  public SaltStyle getSaltStyle() {
    return saltStyle == null ? SaltStyle.NO_SALT : saltStyle;
  }

  public String getExternalSalt() {
    return externalSalt;
  }

  public String getAlgorithmName() {
    return algorithmName;
  }

  public JSONObject getAsSecuredJSONObject() throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(URL_FIELD, url);
    jsonObject.put(AUTHENTICATION_QUERY, authenticationQuery);
    jsonObject.put(USERNAME_FIELD, username);
    jsonObject.put(PASSWORD_FIELD, "");
    jsonObject.put(SALT_STYLE_FIELD, getSaltStyle().name());
    jsonObject.put(EXTERNAL_SALT_FIELD, externalSalt);
    jsonObject.put(ALGORITHM_NAME_FIELD, algorithmName);

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

      String saltStyle = content.optString(SALT_STYLE_FIELD);
      try {
        config.saltStyle = SaltStyle.valueOf(saltStyle);
      } catch (IllegalArgumentException | NullPointerException e) {
        //
      }

      config.externalSalt = content.optString(EXTERNAL_SALT_FIELD);

      if (SaltStyle.COLUMN.equals(config.saltStyle) || SaltStyle.EXTERNAL.equals(config.saltStyle)) {
        config.algorithmName = content.optString(ALGORITHM_NAME_FIELD);
      }
    }

    public JdbcRealmConfig build() {
      return config;
    }

  }
}
