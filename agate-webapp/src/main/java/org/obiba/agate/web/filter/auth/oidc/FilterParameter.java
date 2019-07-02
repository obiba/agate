/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.filter.auth.oidc;

public enum FilterParameter {
  ACTION("action"),
  REDIRECT("redirect"),
  REDIRECT_HASH("redirect_hash"),
  ERROR("error"),
  ERROR_HASH("error_hash"),
  OIDC_PROVIDER_ID("provider");


  private final String param;

  FilterParameter(String value) {
    param = value;
  }

  public String value() {
    return param;
  }
}
