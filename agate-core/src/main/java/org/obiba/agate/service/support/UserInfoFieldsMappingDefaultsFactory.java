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

import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.domain.AgateRealm;

public final class UserInfoFieldsMappingDefaultsFactory {

  private UserInfoFieldsMappingDefaultsFactory() {
  }

  public static JSONObject create() throws JSONException {
    JSONObject model = new JSONObject();
    return model.put(AgateRealm.AGATE_OIDC_REALM.getName(), createOidcDefaults());
  }

  /**
   * See <a href="https://stackoverflow.com/questions/1082050/linking-to-an-external-url-in-javadoc">Standard Claims</a>
   *
   * @return default mapping model
   * @throws JSONException
   */
  private static JSONObject createOidcDefaults() throws JSONException {
    JSONObject model = new JSONObject();
    model.put("username", "preferred_username");
    model.put("email", "email");
    model.put("firstname", "given_name");
    model.put("lastname", "family_name");
    return model;
  }

}
