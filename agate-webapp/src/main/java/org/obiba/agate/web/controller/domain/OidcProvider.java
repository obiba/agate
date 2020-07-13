/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.controller.domain;

import com.google.common.base.Strings;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.oidc.OIDCConfiguration;

import java.util.Map;

public class OidcProvider {

  private final OIDCConfiguration configuration;

  private String title;

  private final String url;

  public OidcProvider(OIDCConfiguration configuration, String locale, String query, String contextPath) {
    this.configuration = configuration;

    Map<String, String> customParams = configuration.getCustomParams();

    // Set localized title if present
    this.title = configuration.getName();
    try {
      if (customParams.containsKey("title")) {
        JSONObject json = new JSONObject(customParams.get("title"));
        if (json.has(locale))
          this.title = json.optString(locale);
      }
    } catch (JSONException ignore) {
      this.title = configuration.getName();
    }

    this.url = contextPath + "/auth/signin/" + configuration.getName() + (Strings.isNullOrEmpty(query) ? "" : "?" + query);
  }

  public String getName() {
    return configuration.getName();
  }

  public String getTitle() {
    return title;
  }

  public String getUrl() {
    return url;
  }
}
