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

public class NotOrphanApplicationException extends BadRequestException {

  private static final long serialVersionUID = 458553132519693383L;

  private NotOrphanApplicationException(String s) {
    super(s);
  }

  public static NotOrphanApplicationException withId(String id) {
    return new NotOrphanApplicationException("Application with id '" + id + "' has user(s) and/or group(s)");
  }

  public static NotOrphanApplicationException withName(String name) {
    return new NotOrphanApplicationException("Application with name '" + name + "' has user(s) and/or group(s)");
  }

}
