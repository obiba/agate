package org.obiba.agate.web.rest;


import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.apache.shiro.authz.UnauthorizedException;
import org.obiba.jersey.exceptionmapper.AbstractErrorDtoExceptionMapper;
import org.obiba.web.model.ErrorDtos;

@Provider
public class UnauthorizedExceptionMapper extends AbstractErrorDtoExceptionMapper<UnauthorizedException> {
  @Override
  protected Response.Status getStatus() {
    return Response.Status.UNAUTHORIZED;
  }

  @Override
  protected ErrorDtos.ClientErrorDto getErrorDto(UnauthorizedException e) {
    return ErrorDtos.ClientErrorDto.newBuilder()
        .setCode(getStatus().getStatusCode())
        .setMessageTemplate("server.error.unauthorized")
        .setMessage(e.getMessage())
        .build();
  }
}
