package org.obiba.agate.config;

import org.obiba.agate.service.OAuth2TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;

/**
 * Scheduled task to refresh OAuth2 access tokens before they expire.
 * Runs every 50 minutes (typical token expiry is 60 minutes).
 */
@Component
@ConditionalOnProperty(name = "spring.mail.oauth2.enabled", havingValue = "true")
public class OAuth2TokenRefresher {

  private static final Logger log = LoggerFactory.getLogger(OAuth2TokenRefresher.class);

  @Inject
  private OAuth2TokenService oauth2TokenService;

  @Inject
  private Environment env;

  /**
   * Refresh token every 50 minutes (3000000 milliseconds)
   * Most OAuth2 access tokens expire after 60 minutes, so this provides a 10-minute safety margin
   */
  @Scheduled(fixedRate = 3000000, initialDelay = 60000)
  public void scheduleTokenRefresh() {
    String authType = env.getProperty("spring.mail.auth-type", "smtp");

    if ("oauth2".equals(authType)) {
      try {
        log.debug("Starting scheduled OAuth2 token refresh");
        oauth2TokenService.refreshAccessToken();
      } catch (Exception e) {
        log.error("Scheduled token refresh failed", e);
      }
    }
  }
}
