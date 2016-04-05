/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.obiba.jersey.exceptionmapper.AbstractErrorDtoExceptionMapper;
import org.obiba.web.model.ErrorDtos;
import org.springframework.dao.DuplicateKeyException;

import com.google.protobuf.GeneratedMessage;

@Provider
public class DuplicateKeyExceptionMapper extends AbstractErrorDtoExceptionMapper<DuplicateKeyException> {

  @Override
  protected Status getStatus() {
    return Status.BAD_REQUEST;
  }

  @Override
  protected GeneratedMessage.ExtendableMessage<?> getErrorDto(DuplicateKeyException e) {
    return ErrorDtos.ClientErrorDto.newBuilder() //
        .setCode(getStatus().getStatusCode()) //
        .setMessageTemplate("server.error.duplicate-key") //
        .setMessage(e.getMessage()) //
        .build();
  }

}
