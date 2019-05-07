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

public class ActiveDirectoryRealmConfigFormBuilder extends BaseRealmConfigFormBuilder {

  private ActiveDirectoryRealmConfigFormBuilder() {
    formSchema = "{" +
      "  \"type\": \"object\"," +
      "  \"properties\": {" +
      "    \"url\": {" +
      "      \"type\": \"string\"," +
      "      \"title\": \"t(realm.ad.url)\"" +
      "    }," +
      "    \"systemUsername\": {" +
      "      \"type\": \"string\"," +
      "      \"title\": \"t(realm.ad.system-username)\"" +
      "    }," +
      "    \"systemPassword\": {" +
      "      \"type\": \"string\"," +
      "      \"title\": \"t(realm.ad.system-password)\"," +
      "      \"format\": \"password\"" +
      "    }," +
      "    \"searchFilter\": {" +
      "      \"type\": \"string\"," +
      "      \"title\": \"t(realm.ad.search-filter)\"" +
      "    }," +
      "    \"searchBase\": {" +
      "      \"type\": \"string\"," +
      "      \"title\": \"t(realm.ad.search-base)\"," +
      "      \"description\": \"t(realm.ad.search-base-help)\"" +
      "    }," +
      "    \"principalSuffix\": {" +
      "      \"type\": \"string\"," +
      "      \"title\": \"t(realm.ad.principal-suffix)\"," +
      "      \"description\": \"t(realm.ad.principal-suffix-help)\"" +
      "    }" +
      "  }," +
      "  \"required\": [" +
      "    \"url\"," +
      "    \"systemUser\"," +
      "    \"systemPassword\"," +
      "    \"searchFilter\"" +
      "  ]" +
      "}";

    formDefinition = "[" +
      "{" +
      "  \"type\": \"section\"," +
      "  \"items\": [" +
      "      \"url\"," +
      "      \"systemUsername\"," +
      "      {" +
      "        \"key\": \"systemPassword\"," +
      "        \"type\": \"password\"" +
      "      }," +
      "      {" +
      "        \"key\": \"searchFilter\"," +
      "        \"placeholder\": \"uid={0}\"" +
      "      }," +
      "      \"searchBase\"," +
      "      \"principalSuffix\"" +
      "    ]" +
      "  }" +
      "]";
  }

  public static ActiveDirectoryRealmConfigFormBuilder newBuilder() {
    return new ActiveDirectoryRealmConfigFormBuilder();
  }
}
