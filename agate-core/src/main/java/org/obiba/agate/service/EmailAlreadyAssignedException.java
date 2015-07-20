/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

public class EmailAlreadyAssignedException extends RuntimeException {

  private final String email;

  public EmailAlreadyAssignedException(String email) {
    super("Email address '" + email + "' is already assigned");
    this.email = email;
  }

  public String getEmail() {
    return email;
  }
}
