/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import java.util.NoSuchElementException;

public class NoSuchRealmConfigException extends NoSuchElementException {

  private static final long serialVersionUID = 493381304855486559L;

  private NoSuchRealmConfigException(String s) {
    super(s);
  }

  public static NoSuchRealmConfigException withName(String name) {
    return new NoSuchRealmConfigException("Realm with name '" + name + "' does not exist");
  }

}
