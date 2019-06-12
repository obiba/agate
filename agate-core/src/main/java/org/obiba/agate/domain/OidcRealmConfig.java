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
import org.obiba.oidc.OIDCConfiguration;

public class OidcRealmConfig extends OIDCConfiguration {
  private static final String NAME_FIELD = "NAME";
  private static final String CLIENT_ID_FIELD = "clientId";
  private static final String SECRET_ID_FIELD = "secret";
  private static final String DISCOVERY_FIELD = "discoveryURI";
  private static final String SCOPE_FIELD = "scope";
  private static final String USE_NONCE_FIELD = "useNonce";
  private static final String CONNECT_TIMEOUT_FIELD = "connectTimeout";
  private static final String READ_TIMEOUT_FIELD = "readTimeout";
  private static final String PREFERRED_JWS_ALGORITHM_FIELD = "preferredJwsAlgorithm";
  private static final String MAX_CLOCK_SKEW_FIELD = "maxClockSkew";
  private static final String PROVIDER_URL_FIELD = "providerUrl";

  private String providerUrl;

  public static OidcRealmConfig.Builder newBuilder(String content) throws JSONException {
    return newBuilder(new JSONObject(content));
  }

  public static OidcRealmConfig.Builder newBuilder(JSONObject content) {
    return new OidcRealmConfig.Builder(content);
  }

  public String getProviderUrl() {
    return providerUrl;
  }

  public void setProviderUrl(String providerUrl) {
    this.providerUrl = providerUrl;
  }

  public static class Builder {

    private final OidcRealmConfig config;

    private Builder(JSONObject content) {
      config = new OidcRealmConfig();
      config.setName(content.optString(NAME_FIELD, "oidc"));
      config.setClientId(content.optString(CLIENT_ID_FIELD));
      config.setSecret(content.optString(SECRET_ID_FIELD));
      config.setDiscoveryURI(content.optString(DISCOVERY_FIELD));
      config.setScope(content.optString(SCOPE_FIELD, "openid"));
      config.setUseNonce(content.optBoolean(USE_NONCE_FIELD, true));
      config.setConnectTimeout(content.optInt(CONNECT_TIMEOUT_FIELD, 0));
      config.setReadTimeout(content.optInt(READ_TIMEOUT_FIELD, 0));
      config.setPreferredJwsAlgorithm(content.optString(PREFERRED_JWS_ALGORITHM_FIELD, null));
      config.setMaxClockSkew(content.optInt(MAX_CLOCK_SKEW_FIELD, 30));
      config.setProviderUrl(content.optString(PROVIDER_URL_FIELD, null));
    }

    public OidcRealmConfig build() {
      return config;
    }
  }


}
