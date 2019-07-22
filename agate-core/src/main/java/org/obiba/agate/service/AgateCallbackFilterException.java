package org.obiba.agate.service;

public class AgateCallbackFilterException extends RuntimeException {

  private String redirectUrl;

  public AgateCallbackFilterException(String message, String redirectUrl) {
    super(message);
    this.redirectUrl = redirectUrl;
  }

  public AgateCallbackFilterException(String message, Exception cause, String redirectUrl) {
    super(message, cause);
    this.redirectUrl = redirectUrl;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }
}
