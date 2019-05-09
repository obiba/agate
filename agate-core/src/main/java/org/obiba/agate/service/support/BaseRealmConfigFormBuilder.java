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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseRealmConfigFormBuilder {
  private static final Logger log = LoggerFactory.getLogger(BaseRealmConfigFormBuilder.class);

  protected String formSchema;

  protected String formDefinition;

  public JSONObject build() {
    JSONObject form = new JSONObject();

    try {
      form.put("schema", new JSONObject(formSchema));
      form.put("definition", new JSONArray(formDefinition));
    } catch (JSONException e) {
      log.error("Invalid JSON format {}", e);
    }

    return form;
  }

}
