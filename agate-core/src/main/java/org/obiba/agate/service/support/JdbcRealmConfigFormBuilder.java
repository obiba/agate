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

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.shiro.realm.jdbc.JdbcRealm.SaltStyle;
import org.json.JSONArray;

public class JdbcRealmConfigFormBuilder extends BaseRealmConfigFormBuilder {

  JdbcRealmConfigFormBuilder() {
      formSchema = "{" +
        "  \"type\": \"object\"," +
        "  \"properties\": {" +
        "    \"url\": {" +
        "      \"type\": \"string\"," +
        "      \"title\": \"t(realm.jdbc.url)\"" +
        "    }," +
        "    \"username\": {" +
        "      \"type\": \"string\"," +
        "      \"title\": \"t(realm.jdbc.username)\"" +
        "    }," +
        "    \"password\": {" +
        "      \"type\": \"string\"," +
        "      \"title\": \"t(realm.jdbc.password)\"," +
        "      \"format\": \"password\"" +
        "    }," +
        "    \"authenticationQuery\": {" +
        "      \"type\": \"string\"," +
        "      \"title\": \"t(realm.jdbc.authentication-query)\"," +
        "      \"description\": \"t(realm.jdbc.authentication-query-help)\"" +
        "    }," +
        "    \"saltStyle\": {" +
        "      \"type\": \"string\"," +
        "      \"title\": \"t(realm.jdbc.salt-style)\"," +
        "        \"default\": \"" + SaltStyle.NO_SALT + "\"," +
        "        \"enum\": " + saltStyleEnum() +
        "    }," +
        "    \"externalSalt\": {" +
        "      \"type\": \"string\"," +
        "      \"title\": \"t(realm.jdbc.external-salt)\"," +
        "      \"description\": \"t(realm.jdbc.external-salt-help)\"" +
        "    }," +
        "    \"algorithmName\": {" +
        "      \"type\": \"string\"," +
        "      \"title\": \"t(realm.jdbc.algorithm-name)\"," +
        "      \"description\": \"t(realm.jdbc.algorithm-name-help)\"" +
        "    }" +
        "  }," +
        "  \"required\": [" +
        "    \"url\"," +
        "    \"username\"," +
        "    \"password\"," +
        "    \"authenticationQuery\"" +
        "  ]" +
        "}";

      formDefinition = "[" +
        "  {" +
        "    \"type\": \"section\"," +
        "    \"items\": [" +
        "      {" +
        "        \"key\": \"url\"," +
        "        \"placeholder\": \"jdbc:mariadb://localhost:3306/users_db\"" +
        "      }," +
        "      \"username\"," +
        "      {" +
        "        \"key\": \"password\"," +
        "        \"type\": \"password\"" +
        "      }," +
        "      {" +
        "        \"type\": \"section\"," +
        "        \"items\": [" +
        "          {" +
        "            \"key\": \"authenticationQuery\"," +
        "            \"placeholder\": \"select password from users where username = ?\"" +
        "          }," +
        "          {" +
        "            \"helpvalue\": \"<div class='help-block'>t(realm.jdbc.salt-style-column-help)</div>\"," +
        "            \"type\": \"help\"," +
        "            \"condition\": \"model.saltStyle === '" + SaltStyle.COLUMN + "'\"" +
        "          }," +
        "          {" +
        "            \"key\": \"saltStyle\"," +
        "            \"type\": \"select\"" +
        "          }," +
        "          {" +
        "            \"key\": \"externalSalt\"," +
        "            \"condition\": \"model.saltStyle === '" + SaltStyle.EXTERNAL + "'\"" +
        "          }," +
        "          {" +
        "            \"key\": \"algorithmName\"," +
        "            \"placeholder\": \"SHA-256\"," +
        "            \"condition\": \"model.saltStyle === '" + SaltStyle.EXTERNAL + "' || model.saltStyle === '" + SaltStyle.COLUMN + "'\"" +
        "          }" +
        "        ]" +
        "      }" +
        "    ]" +
        "  }" +
        "]";
  }

  private String saltStyleEnum() {
    return new JSONArray(Stream.of(SaltStyle.values()).map(SaltStyle::toString).collect(Collectors.toList())).toString();
  }

  public static JdbcRealmConfigFormBuilder newBuilder() {
    return new JdbcRealmConfigFormBuilder();
  }
}
