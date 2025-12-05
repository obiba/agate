package org.obiba.agate.config;

import org.obiba.agate.service.OAuth2TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import java.time.Instant;

/**
 * Scheduled task to refresh OAuth2 access tokens before they expire.
 * Dynamically schedules refreshes based on the actual token expiry time from the OAuth2 provider.
 */
@Component
@ConditionalOnProperty(name = "spring.mail.auth-type", havingValue = "oauth2")
public class OAuth2TokenRefresher {

  private static final Logger log = LoggerFactory.getLogger(OAuth2TokenRefresher.class);

  // Initial delay before first token refresh (1 minute in milliseconds)
  private static final long INITIAL_REFRESH_DELAY_MS = 60000;

  // Retry delay on error (5 minutes in seconds)
  private static final long ERROR_RETRY_DELAY_SECONDS = 300;

  @Inject
  private OAuth2TokenService oauth2TokenService;

  @Inject
  @Qualifier("taskScheduler")
  private TaskScheduler taskScheduler;

  @Inject
  private JavaMailSenderImpl javaMailSender;

  /**
   * Start the token refresh cycle when application is fully started
   */
  @EventListener(ApplicationReadyEvent.class)
  public void initialize() {
    // Schedule initial refresh to allow application to fully start
    Instant firstRefresh = Instant.now().plusMillis(INITIAL_REFRESH_DELAY_MS);

    log.info("OAuth2 token refresher initialized. First refresh scheduled at {}", firstRefresh);
    taskScheduler.schedule(this::refreshAndScheduleNext, firstRefresh);
  }

  /**
   * Refresh the token and schedule the next refresh based on token expiry
   */
  private void refreshAndScheduleNext() {
    try {
      log.debug("Starting scheduled OAuth2 token refresh");
      oauth2TokenService.refreshAccessToken();

      // Update JavaMailSender with the new access token
      String newAccessToken = oauth2TokenService.getAccessToken();
      javaMailSender.setPassword(newAccessToken);
      log.debug("Updated JavaMailSender with refreshed OAuth2 token");

      // Get the token expiry time (already includes 5-minute safety buffer)
      long expiryTime = oauth2TokenService.getTokenExpiryTime();

      if (expiryTime > 0) {
        Instant nextRefresh = Instant.ofEpochMilli(expiryTime);
        long delaySeconds = (expiryTime - System.currentTimeMillis()) / 1000;

        log.info("Next OAuth2 token refresh scheduled at {} (in {} seconds)", nextRefresh, delaySeconds);
        taskScheduler.schedule(this::refreshAndScheduleNext, nextRefresh);
      } else {
        log.warn("Token expiry time not available, will retry in {} seconds", ERROR_RETRY_DELAY_SECONDS);
        taskScheduler.schedule(this::refreshAndScheduleNext, Instant.now().plusSeconds(ERROR_RETRY_DELAY_SECONDS));
      }

    } catch (Exception e) {
      log.error("Scheduled token refresh failed, will retry in {} seconds", ERROR_RETRY_DELAY_SECONDS, e);
      // On error, retry after delay
      taskScheduler.schedule(this::refreshAndScheduleNext, Instant.now().plusSeconds(ERROR_RETRY_DELAY_SECONDS));
    }
  }
}
