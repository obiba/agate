package org.obiba.agate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import jakarta.inject.Inject;
import java.util.Map;

@Service
public class OAuth2TokenService {

  private static final Logger log = LoggerFactory.getLogger(OAuth2TokenService.class);

  @Inject
  private Environment env;

  private final RestTemplate restTemplate = new RestTemplate();

  private volatile String currentAccessToken;
  private volatile long tokenExpiryTime;

  // Token refresh safety buffer (5 minutes in seconds)
  private static final int TOKEN_REFRESH_BUFFER_SECONDS = 300;

  /**
   * Get current access token, refreshing if necessary
   */
  public synchronized String getAccessToken() {
    if (currentAccessToken == null || isTokenExpired()) {
      refreshAccessToken();
    }
    return currentAccessToken;
  }

  /**
   * Force refresh the access token
   */
  public synchronized void refreshAccessToken() {
    try {
      log.info("Refreshing OAuth2 access token...");

      String tokenUri = env.getProperty("spring.mail.oauth2.token-uri");
      String clientId = env.getProperty("spring.mail.oauth2.client-id");
      String clientSecret = env.getProperty("spring.mail.oauth2.client-secret");
      String refreshToken = env.getProperty("spring.mail.oauth2.refresh-token");
      String scope = env.getProperty("spring.mail.oauth2.scope");

      if (tokenUri == null || clientId == null || clientSecret == null || refreshToken == null) {
        throw new IllegalStateException("OAuth2 configuration is incomplete");
      }

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
      body.add("client_id", clientId);
      body.add("client_secret", clientSecret);
      body.add("refresh_token", refreshToken);
      body.add("grant_type", "refresh_token");
      if (scope != null) {
        body.add("scope", scope);
      }

      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
      ResponseEntity<Map> response = restTemplate.exchange(tokenUri, HttpMethod.POST, request, Map.class);

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        Map<String, Object> responseBody = response.getBody();
        currentAccessToken = (String) responseBody.get("access_token");
        Integer expiresIn = (Integer) responseBody.get("expires_in");

        if (currentAccessToken != null && expiresIn != null) {
          // Set expiry time with safety buffer before actual expiry
          tokenExpiryTime = System.currentTimeMillis() + ((expiresIn - TOKEN_REFRESH_BUFFER_SECONDS) * 1000L);
          log.info("OAuth2 access token refreshed successfully. Expires in {} seconds", expiresIn);
        } else {
          throw new IllegalStateException("Invalid token response: missing access_token or expires_in");
        }
      } else {
        throw new IllegalStateException("Failed to refresh token. HTTP status: " + response.getStatusCode());
      }

    } catch (Exception e) {
      log.error("Failed to refresh OAuth2 access token", e);
      throw new RuntimeException("OAuth2 token refresh failed", e);
    }
  }

  /**
   * Check if current token is expired or about to expire
   */
  private boolean isTokenExpired() {
    if (tokenExpiryTime == 0) {
      return true;
    }
    return System.currentTimeMillis() >= tokenExpiryTime;
  }

  /**
   * Get the token expiry time (milliseconds since epoch)
   * This is already adjusted with a 5-minute safety buffer
   */
  public long getTokenExpiryTime() {
    return tokenExpiryTime;
  }

  /**
   * Clear cached token (for testing/debugging)
   */
  public synchronized void clearToken() {
    currentAccessToken = null;
    tokenExpiryTime = 0;
  }
}
