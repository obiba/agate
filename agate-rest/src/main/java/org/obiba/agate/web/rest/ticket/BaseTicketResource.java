/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.ticket;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;

import org.obiba.agate.service.ApplicationService;

public class BaseTicketResource {

  @Inject
  private ApplicationService applicationService;

  protected void validateApplication(String name, String key) {
    if(!applicationService.isValid(name, key)) throw new ForbiddenException();
  }
}
