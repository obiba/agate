/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import jakarta.ws.rs.BadRequestException;

public class InvalidRealmConfigException extends BadRequestException {

  private static final long serialVersionUID = -2930476386563210128L;

  private final String messageKey;

  private final String name;

  private InvalidRealmConfigException(String messageKey, String name, String message) {
    super(message);
    this.messageKey = messageKey;
    this.name = name;
  }

  public String getMessageKey() {
    return messageKey;
  }

  public String getName() {
    return name;
  }

  public static InvalidRealmConfigException alreadyExists(String name) {
    return new InvalidRealmConfigException("already-exists", name, "Realm already exists: " + name);
  }

  public static InvalidRealmConfigException invalidName(String name) {
    return new InvalidRealmConfigException("invalid-name", name,
      "Realm name should only contain alphanumerical characters, hyphens and underscores: " + name);
  }

  public static InvalidRealmConfigException contentRequired(String name) {
    return new InvalidRealmConfigException("content-required", name, "Realm configuration is required: " + name);
  }

}
