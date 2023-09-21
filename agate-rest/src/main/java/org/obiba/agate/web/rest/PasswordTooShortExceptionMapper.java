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

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.obiba.agate.service.PasswordTooShortException;
import org.obiba.jersey.exceptionmapper.AbstractErrorDtoExceptionMapper;
import org.obiba.web.model.ErrorDtos;

import com.google.protobuf.GeneratedMessage;


@Provider
public class PasswordTooShortExceptionMapper extends AbstractErrorDtoExceptionMapper<PasswordTooShortException> {

  @Override
  protected Response.Status getStatus() {
    return Response.Status.BAD_REQUEST;
  }

  @Override
  protected ErrorDtos.ClientErrorDto getErrorDto(PasswordTooShortException e) {
    return ErrorDtos.ClientErrorDto.newBuilder() //
      .setCode(getStatus().getStatusCode()) //
      .addArguments(e.getMinSize() + "") //
      .setMessageTemplate("server.error.password.too-short") //
      .setMessage(e.getMessage()) //
      .build();
  }

}
