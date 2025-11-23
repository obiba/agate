package org.obiba.agate.web.rest;


import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.obiba.agate.web.rest.user.UserCreationException;
import org.obiba.jersey.exceptionmapper.AbstractErrorDtoExceptionMapper;
import org.obiba.web.model.ErrorDtos;

@Provider
public class UserCreationExceptionMapper extends AbstractErrorDtoExceptionMapper<UserCreationException> {
  @Override
  protected Response.Status getStatus() {
    return Response.Status.BAD_REQUEST;
  }

  @Override
  protected ErrorDtos.ClientErrorDto getErrorDto(UserCreationException e) {
    return ErrorDtos.ClientErrorDto.newBuilder()
        .setCode(getStatus().getStatusCode())
        .setMessageTemplate(e.getMessageTemplate())
        .addAllArguments(e.getArguments())
        .setMessage(e.getMessage())
        .build();
  }
}
