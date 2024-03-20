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

import org.obiba.agate.service.PasswordTooLongException;
import org.obiba.jersey.exceptionmapper.AbstractErrorDtoExceptionMapper;
import org.obiba.web.model.ErrorDtos;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;


@Provider
public class PasswordTooLongExceptionMapper extends AbstractErrorDtoExceptionMapper<PasswordTooLongException> {

  @Override
  protected Response.Status getStatus() {
    return Response.Status.BAD_REQUEST;
  }

  @Override
  protected ErrorDtos.ClientErrorDto getErrorDto(PasswordTooLongException e) {
    return ErrorDtos.ClientErrorDto.newBuilder() //
        .setCode(getStatus().getStatusCode()) //
        .addArguments(e.getMaxSize() + "") //
        .setMessageTemplate("server.error.password.too-long") //
        .setMessage(e.getMessage()) //
        .build();
  }

}
