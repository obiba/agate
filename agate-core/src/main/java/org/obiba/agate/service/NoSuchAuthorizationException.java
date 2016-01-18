/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import java.util.NoSuchElementException;

public class NoSuchAuthorizationException extends NoSuchElementException {

  private static final long serialVersionUID = 5478617900008621543L;

  private NoSuchAuthorizationException(String s) {
    super(s);
  }

  public static NoSuchAuthorizationException withId(String id) {
    return new NoSuchAuthorizationException("Authorization with ID '" + id + "' does not exist");
  }

  public static NoSuchAuthorizationException withCode(String code) {
    return new NoSuchAuthorizationException("Authorization with code '" + code + "' does not exist");
  }


  public static NoSuchAuthorizationException withUsernameAndApplication(String username, String application) {
    return new NoSuchAuthorizationException(
      "Authorization with username '" + username + "' and application '" + application + "' does not exist");
  }

}
