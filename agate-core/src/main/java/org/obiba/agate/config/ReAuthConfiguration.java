package org.obiba.agate.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "login.re-auth")
public class ReAuthConfiguration {

  private static final Logger log = LoggerFactory.getLogger(ReAuthConfiguration.class);

  private int timeout;

  private List<String> endpoints;

  // Getters and setters
  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public List<String> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(List<String> endpoints) {
    this.endpoints = endpoints;
  }

  public boolean appliesTo(String method, String path) {
    log.debug("Re-auth request evaluated: {}:{}", method, path);
    for (String endpoint : endpoints) {
      String[] parts = endpoint.split(":");
      if (parts.length == 2) {
        String endpointMethod = parts[0];
        String endpointPath = parts[1];
        if (!endpointMethod.equals(method)) continue;
        if (endpointPath.equals(path)) {
          return true;
        } else if (endpointPath.endsWith("/**") && path.startsWith(endpointPath.substring(0, endpointPath.length() - 2))) {
          return true;
        }
      }
    }
    return false;
  }
}
