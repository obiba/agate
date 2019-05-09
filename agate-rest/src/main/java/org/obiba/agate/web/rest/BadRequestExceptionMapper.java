/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.obiba.jersey.exceptionmapper.AbstractErrorDtoExceptionMapper;
import org.obiba.web.model.ErrorDtos;

import com.google.protobuf.GeneratedMessage;

@Provider
public class BadRequestExceptionMapper extends AbstractErrorDtoExceptionMapper<BadRequestException> {

  @Override
  protected Status getStatus() {
    return Status.BAD_REQUEST;
  }

  @Override
  protected GeneratedMessage.ExtendableMessage<?> getErrorDto(BadRequestException e) {
    return ErrorDtos.ClientErrorDto.newBuilder() //
        .setCode(getStatus().getStatusCode()) //
        .setMessageTemplate("server.error.bad-request") //
        .setMessage(e.getMessage()) //
        .build();
  }

}
