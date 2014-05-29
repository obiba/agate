/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import java.util.NoSuchElementException;

public class NoSuchTicketException extends NoSuchElementException {

  private static final long serialVersionUID = 458553132519693383L;

  private NoSuchTicketException(String s) {
    super(s);
  }

  public static NoSuchTicketException withId(String id) {
    return new NoSuchTicketException("Ticket with id '" + id + "' does not exist");
  }

}
