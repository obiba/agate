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

import com.google.common.collect.Maps;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.shiro.realm.OIDCRealm;

import java.util.Map;

public class OidcRealmConfig extends OIDCConfiguration {
  private static final String NAME_FIELD = "NAME";
  private static final String CLIENT_ID_FIELD = "clientId";
  private static final String SECRET_ID_FIELD = "secret";
  private static final String DISCOVERY_FIELD = "discoveryURI";
  private static final String SCOPE_FIELD = "scope";
  private static final String USE_NONCE_FIELD = "useNonce";
  private static final String CONNECT_TIMEOUT_FIELD = "connectTimeout";
  private static final String READ_TIMEOUT_FIELD = "readTimeout";
  private static final String MAX_CLOCK_SKEW_FIELD = "maxClockSkew";
  private static final String PROVIDER_URL_FIELD = "providerUrl";
  private static final String USERNAME_MAPPING_FIELD = "username";
  private static final String USERNAME_CLAIM = "usernameClaim";
  private static final String GROUPS_CLAIM = "groupsClaim";
  private static final String GROUPS_JS = "groupsJS";

  public static OidcRealmConfig.Builder newBuilder(String content) throws JSONException {
    return newBuilder(new JSONObject(content));
  }

  private static OidcRealmConfig.Builder newBuilder(JSONObject content) {
    return new OidcRealmConfig.Builder(content);
  }

  public JSONObject getAsSecuredJSONObject() throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(CLIENT_ID_FIELD, getClientId());
    jsonObject.put(SECRET_ID_FIELD, getSecret());
    jsonObject.put(DISCOVERY_FIELD, getDiscoveryURI());
    jsonObject.put(SCOPE_FIELD, getScope());
    jsonObject.put(USE_NONCE_FIELD, isUseNonce());
    jsonObject.put(CONNECT_TIMEOUT_FIELD, getConnectTimeout());

    if (getCustomParams().containsKey(PROVIDER_URL_FIELD)) {
      jsonObject.put(PROVIDER_URL_FIELD, getCustomParams().get(PROVIDER_URL_FIELD));
    }

    if (getCustomParams().containsKey(GROUPS_CLAIM)) {
      jsonObject.put(GROUPS_CLAIM, getCustomParams().get(GROUPS_CLAIM));
    }

    if (getCustomParams().containsKey(GROUPS_JS)) {
      jsonObject.put(GROUPS_JS, getCustomParams().get(GROUPS_JS));
    }

    return jsonObject;
  }

  public static class Builder {

    private final OidcRealmConfig config;

    private Builder(JSONObject content) {
      config = new OidcRealmConfig();
      Map<String, String> customParameters = Maps.newHashMap();

      config.setCustomParams(customParameters);
      config.setName(content.optString(NAME_FIELD, "oidc"));
      config.setClientId(content.optString(CLIENT_ID_FIELD));
      config.setSecret(content.optString(SECRET_ID_FIELD));
      config.setDiscoveryURI(content.optString(DISCOVERY_FIELD));
      config.setScope(content.optString(SCOPE_FIELD, "openid"));
      config.setUseNonce(content.optBoolean(USE_NONCE_FIELD, true));
      config.setConnectTimeout(content.optInt(CONNECT_TIMEOUT_FIELD, 0));
      config.setReadTimeout(content.optInt(READ_TIMEOUT_FIELD, 0));
      config.setMaxClockSkew(content.optInt(MAX_CLOCK_SKEW_FIELD, 30));

      if (content.has(PROVIDER_URL_FIELD)) {
        customParameters.put(PROVIDER_URL_FIELD, content.optString(PROVIDER_URL_FIELD));
      }

      if (content.has(GROUPS_CLAIM)) {
        customParameters.put(GROUPS_CLAIM, content.optString(GROUPS_CLAIM));
      }

      if (content.has(GROUPS_JS)) {
        customParameters.put(GROUPS_JS, content.optString(GROUPS_JS));
      }
    }

    public Builder setUserInfoMapping(Map<String, String> userInfoMapping) {
      if (userInfoMapping.containsKey(USERNAME_MAPPING_FIELD)) {
        config.setCustomParam(USERNAME_CLAIM, userInfoMapping.get(USERNAME_MAPPING_FIELD));
      }
      return this;
    }

    public OidcRealmConfig build() {
      return config;
    }
  }


}
