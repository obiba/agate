/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import javax.ws.rs.BadRequestException;

public class NotOrphanGroupException extends BadRequestException {

  private static final long serialVersionUID = 458553132519693383L;

  private NotOrphanGroupException(String s) {
    super(s);
  }

  public static NotOrphanGroupException withId(String id) {
    return new NotOrphanGroupException("Group with id '" + id + "' has user(s)");
  }

  public static NotOrphanGroupException withName(String name) {
    return new NotOrphanGroupException("Group with name '" + name + "' has user(s)");
  }

}
