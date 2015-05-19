package org.obiba.agate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="client")
public class ClientConfiguration {
  private String reCaptchaKey;

  public String getReCaptchaKey() {
    return reCaptchaKey;
  }

  public void setReCaptchaKey(String reCaptchaKey) {
    this.reCaptchaKey = reCaptchaKey;
  }
}
