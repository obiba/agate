package org.obiba.agate.config;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AgateSessionListener implements HttpSessionListener {
  private static final Logger logger = LoggerFactory.getLogger(AgateSessionListener.class);

  @Override
  public void sessionCreated(HttpSessionEvent event) {
    logger.info("🔹 Session Created: ID={}", event.getSession().getId());
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    logger.warn("⚠️ Session Destroyed: ID={}, Possible Cause: Timeout or Explicit Invalidation",
        event.getSession().getId());
  }
}
