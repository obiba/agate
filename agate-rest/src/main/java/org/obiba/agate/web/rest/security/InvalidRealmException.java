/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.security;

import jakarta.ws.rs.ForbiddenException;

public class InvalidRealmException extends ForbiddenException {

  public InvalidRealmException() {
    super("Invalid user realm");
  }
}
