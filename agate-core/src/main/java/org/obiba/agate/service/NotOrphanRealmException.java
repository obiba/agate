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

import javax.ws.rs.BadRequestException;

public class NotOrphanRealmException extends BadRequestException {

  private static final long serialVersionUID = 8448501848478895251L;

  private NotOrphanRealmException(String s) {
    super(s);
  }

  public static NotOrphanRealmException withName(String name) {
    return new NotOrphanRealmException("Realm with name '" + name + "' has user(s)");
  }

}
