/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.domain;

/**
 * User profile status.
 */
public enum UserStatus {

  ACTIVE,   // User operational
  PENDING,  // User submitted to administrator approval
  APPROVED, // User was approved by administrator but still require user confirmation (email, password reset)
  INACTIVE  // User temporary banned

}
