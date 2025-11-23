package org.obiba.agate.web.rest.user;

import jakarta.ws.rs.BadRequestException;

import java.util.Arrays;

public class UserCreationException extends BadRequestException {

  private final String messageTemplate;
  
  private final String[] arguments;

  public UserCreationException(String message, String messageTemplate, String... arguments) {
    super(message);
    this.messageTemplate = messageTemplate;
    this.arguments = arguments == null ? new String[0] : arguments;
  }

  public String getMessageTemplate() {
    return messageTemplate;
  }

  public Iterable<String> getArguments() {
    return Arrays.asList(arguments);
  }
}
