/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest;

import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;
import org.obiba.agate.service.NotOrphanRealmException;
import org.obiba.jersey.exceptionmapper.AbstractErrorDtoExceptionMapper;
import org.obiba.web.model.ErrorDtos;

@Provider
public class NotOrphanRealmExceptionMapper extends AbstractErrorDtoExceptionMapper<NotOrphanRealmException> {

  @Override
  protected Status getStatus() {
    return Status.BAD_REQUEST;
  }

  @Override
  protected ErrorDtos.ClientErrorDto getErrorDto(NotOrphanRealmException e) {
    return ErrorDtos.ClientErrorDto.newBuilder() //
        .setCode(getStatus().getStatusCode()) //
        .setMessageTemplate("server.error.realm.not-orphan") //
        .setMessage(e.getMessage()) //
        .build();
  }

}
