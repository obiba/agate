package org.obiba.agate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfiguration {

  @Bean
  public RestTemplate getRestTemplate() {
    return new RestTemplate();
  }
}
