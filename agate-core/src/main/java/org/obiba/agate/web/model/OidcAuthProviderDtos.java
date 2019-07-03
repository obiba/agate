/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.web.model.OIDCDtos;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Component
public class OidcAuthProviderDtos {

  @NotNull
  OIDCDtos.OIDCAuthProviderSummaryDto asSummaryDto(@NotNull OIDCConfiguration configuration, @Nullable String locale) {
    String name = configuration.getName();
    Map<String, String> customParams = configuration.getCustomParams();

    OIDCDtos.OIDCAuthProviderSummaryDto.Builder builder = OIDCDtos.OIDCAuthProviderSummaryDto.newBuilder()
      .setName(name)
      .setTitle(name);

    // Default value
    if (customParams.containsKey("providerUrl")) {
      builder.setProviderUrl(customParams.get("providerUrl"));
    }

    // Set localized title if present
    try {
      if (customParams.containsKey("title")) {
        JSONObject json = new JSONObject(customParams.get("title"));
        if (json.has(locale)) builder.setTitle(json.optString(locale));
      }
    } catch (JSONException ignore) {
    }

    return builder.build();
  }

}
