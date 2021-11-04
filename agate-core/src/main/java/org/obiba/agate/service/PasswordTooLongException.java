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

public class PasswordTooLongException extends RuntimeException {
  private final int maxSize;

  public PasswordTooLongException(int maxSize) {
    super(String.format("Password is longer than %d characters", maxSize));
    this.maxSize = maxSize;
  }

  public int getMaxSize() {
    return maxSize;
  }
}



