package org.obiba.agate.config;

import com.beust.jcommander.internal.Lists;
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

  private List<Endpoint> endpointList;

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
    this.endpointList = endpoints == null ? Lists.newArrayList() : endpoints.stream().map(ep -> {
      String[] parts = ep.split(":");
      if (parts.length == 2) {
        return new Endpoint(parts[0].toUpperCase(), parts[1]);
      } else {
        throw new IllegalArgumentException("Invalid endpoint format: " + ep);
      }
    }).toList();
  }

  public boolean appliesTo(String method, String path) {
    log.debug("Re-auth request evaluated: {}:{}", method, path);
    if (endpointList != null) {
      for (Endpoint endpoint : endpointList) {
        if (endpoint.appliesTo(method.toUpperCase(), path)) {
          log.debug("Re-auth required for request: {}:{}", method, path);
          return true;
        }
      }
    }
    return false;
  }

  private record Endpoint(String method, String path) {

    public boolean appliesTo(String requestMethod, String requestPath) {
      if (!requestMethod.equals(this.method)) return false;
      if (this.path.equals(requestPath)) return true;
      // check for wildcards '*'
      if (this.path.contains("/*")) {
        String[] patternParts = this.path.split("/");
        String[] requestParts = requestPath.split("/");
        if (patternParts.length != requestParts.length) return false;
        for (int i = 0; i < patternParts.length; i++) {
          if (patternParts[i].equals("*") && requestParts[i].isEmpty()) {
            return false;
          }
          if (!patternParts[i].equals("*") && !patternParts[i].equals(requestParts[i])) {
            return false;
          }
        }
        return true;
      }
      return false;
    }
  }
}
