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

import org.obiba.agate.domain.RealmStatus;

public class OidcRealmConfigFormBuilder extends BaseRealmConfigFormBuilder {
  private OidcRealmConfigFormBuilder() {

    formSchema = "{" +
      "  \"type\": \"object\"," +
      "  \"properties\": {" +
      "    \"clientId\": {" +
      "      \"type\": \"string\"," +
      "      \"title\": \"t(realm.oidc.client-id)\"," +
      "      \"description\": \"t(realm.oidc.client-id-help)\"" +
      "    }," +
      "    \"clientSecret\": {" +
      "      \"type\": \"string\"," +
      "      \"title\": \"t(realm.oidc.client-secret)\"," +
      "      \"format\": \"password\"," +
      "      \"description\": \"t(realm.oidc.client-secret-help)\"" +
      "    }," +
      "    \"discoveryURI\": {" +
      "      \"type\": \"string\"," +
      "      \"title\": \"t(realm.oidc.discovery-uri)\"," +
      "      \"description\": \"t(realm.oidc.discovery-uri-help)\"" +
      "    }," +
      "    \"scope\": {" +
      "      \"type\": \"string\"," +
      "      \"title\": \"t(realm.oidc.scope)\"," +
      "      \"default\": \"openid\"," +
      "      \"description\": \"t(realm.oidc.scope-help)\"" +
      "    }," +
      "    \"useNonce\": {" +
      "      \"type\": \"boolean\"," +
      "      \"title\": \"t(realm.oidc.use-nonce)\"," +
      "      \"default\": " + true + "," +
      "      \"description\": \"t(realm.oidc.use-nonce-help)\"" +
      "    }," +
      "    \"connectTimeout\": {" +
      "      \"type\": \"integer\"," +
      "      \"title\": \"t(realm.oidc.connect-timeout)\"," +
      "      \"default\": " + 0 + "," +
      "      \"description\": \"t(realm.oidc.connect-timeout-help)\"" +
      "    }," +
      "    \"readTimeout\": {" +
      "      \"type\": \"integer\"," +
      "      \"title\": \"t(realm.oidc.read-timeout)\"," +
      "      \"default\": " + 0 + " ," +
      "      \"description\": \"t(realm.oidc.read-timeout-help)\"" +
      "    }," +
      "    \"providerUrl\": {" +
      "      \"type\": \"string\"," +
      "      \"title\": \"t(realm.oidc.provider-url)\"," +
      "      \"description\": \"t(realm.oidc.provider-url-help)\"" +
      "    }" +
      "  }," +
      "  \"required\": [" +
      "    \"clientId\"," +
      "    \"clientSecret\"," +
      "    \"discoveryURI\"" +
      "  ]" +
      "}";

    formDefinition = "[" +
      "{" +
      "  \"type\": \"section\"," +
      "  \"items\": [" +
      "      \"clientId\"," +
      "      \"clientSecret\"," +
      "      \"discoveryURI\"," +
      "      \"providerUrl\"," +
      "      \"scope\"," +
      "      \"useNonce\"," +
      "      \"connectTimeout\"," +
      "      \"readTimeout\"" +
      "    ]" +
      "  }" +
      "]";
  }

  public static OidcRealmConfigFormBuilder newBuilder() {
    return new OidcRealmConfigFormBuilder();
  }
}
