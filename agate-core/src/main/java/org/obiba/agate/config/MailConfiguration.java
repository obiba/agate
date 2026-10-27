/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.config;

import java.util.Properties;

import org.obiba.agate.service.OAuth2TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import jakarta.inject.Inject;

@Configuration
public class MailConfiguration implements EnvironmentAware {

  @Inject
  private OAuth2TokenService oauth2TokenService;

  private static final String ENV_SPRING_MAIL = "spring.mail";

  private static final String DEFAULT_HOST = "127.0.0.1";

  private static final String PROP_HOST = "host";

  private static final String DEFAULT_PROP_HOST = "localhost";

  private static final String PROP_PORT = "port";

  private static final String PROP_USER = "user";

  private static final String PROP_PASSWORD = "password";

  private static final String PROP_PROTO = "protocol";

  private static final String PROP_TLS = "tls";

  private static final String PROP_SSL_ENABLED = "ssl.enabled";

  private static final String PROP_AUTH = "auth";

  private static final String PROP_CONNECTION_TIMEOUT = "connection-timeout";

  private static final String PROP_TIMEOUT = "timeout";

  private static final int DEFAULT_CONNECTION_TIMEOUT = 10000;

  private static final int DEFAULT_TIMEOUT = 30000;

  // JavaMail per-protocol properties, to be prefixed with "mail.<protocol>."
  private static final String MAIL_AUTH = "auth";

  private static final String MAIL_AUTH_MECHANISMS = "auth.mechanisms";

  private static final String MAIL_STARTTLS = "starttls.enable";

  private static final String MAIL_SSL_ENABLE = "ssl.enable";

  private static final String MAIL_SSL_PROTOCOLS = "ssl.protocols";

  private static final String MAIL_CONNECTION_TIMEOUT = "connectiontimeout";

  private static final String MAIL_TIMEOUT = "timeout";

  private static final String MAIL_WRITE_TIMEOUT = "writetimeout";

  private static final String PROP_TRANSPORT_PROTO = "mail.transport.protocol";

  private static final String DEFAULT_PROP_MAIL_PROTOCOLS = "TLSv1.2 TLSv1.3";

  private static final Logger log = LoggerFactory.getLogger(MailConfiguration.class);

  private Environment environment;

  public MailConfiguration() {
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Bean
  public JavaMailSenderImpl javaMailSender() {
    log.debug("Configuring mail server");
    String host = environment.getProperty(String.format("%s.%s", ENV_SPRING_MAIL, PROP_HOST), DEFAULT_PROP_HOST);
    int port = environment.getProperty(String.format("%s.%s", ENV_SPRING_MAIL, PROP_PORT), Integer.class, 0);
    String user = environment.getProperty(String.format("%s.%s", ENV_SPRING_MAIL, PROP_USER));
    String password = environment.getProperty(String.format("%s.%s", ENV_SPRING_MAIL, PROP_PASSWORD));

    // Check authentication type
    String authType = environment.getProperty(String.format("%s.auth-type", ENV_SPRING_MAIL), "smtp");

    JavaMailSenderImpl sender = new JavaMailSenderImpl();
    if(host != null && !host.isEmpty()) {
      sender.setHost(host);
    } else {
      log.warn("Warning! Your SMTP server is not configured. We will try to use one on localhost.");
      log.debug("Did you configure your SMTP settings in your application.yml?");
      sender.setHost(DEFAULT_HOST);
    }
    sender.setPort(port);
    sender.setUsername(user);

    Properties sendProperties = buildMailProperties(environment);
    String protocol = sendProperties.getProperty(PROP_TRANSPORT_PROTO);
    String prefix = mailPropertyPrefix(protocol);

    if ("oauth2".equalsIgnoreCase(authType)) {
      // OAuth2 authentication
      log.info("Configuring OAuth2 authentication for email");
      configureOAuth2(sender, sendProperties, prefix);

      // Register JavaMailSender with OAuth2TokenService for automatic token updates
      oauth2TokenService.setJavaMailSender(sender);
    } else {
      // Traditional SMTP authentication (default)
      log.info("Configuring SMTP authentication for email");
      sender.setPassword(password);
    }

    log.info("Mail server {}:{} (protocol={}, starttls={}, ssl={}, connection-timeout={}ms, timeout={}ms)",
      sender.getHost(), port, protocol,
      sendProperties.getProperty(prefix + MAIL_STARTTLS),
      "smtps".equals(protocol) || Boolean.parseBoolean(sendProperties.getProperty(prefix + MAIL_SSL_ENABLE)),
      sendProperties.getProperty(prefix + MAIL_CONNECTION_TIMEOUT), sendProperties.getProperty(prefix + MAIL_TIMEOUT));

    sender.setJavaMailProperties(sendProperties);
    return sender;
  }

  /**
   * Build the JavaMail properties from the spring.mail settings. Per-protocol properties are named after
   * the transport protocol: "mail.smtp.*" for smtp, "mail.smtps.*" for smtps (implicit SSL).
   */
  static Properties buildMailProperties(Environment environment) {
    String protocol = environment.getProperty(String.format("%s.%s", ENV_SPRING_MAIL, PROP_PROTO), "smtp").trim().toLowerCase();
    if (protocol.isEmpty()) protocol = "smtp";
    Boolean tls = environment.getProperty(String.format("%s.%s", ENV_SPRING_MAIL, PROP_TLS), Boolean.class, false);
    Boolean ssl = environment.getProperty(String.format("%s.%s", ENV_SPRING_MAIL, PROP_SSL_ENABLED), Boolean.class, false);
    Boolean auth = environment.getProperty(String.format("%s.%s", ENV_SPRING_MAIL, PROP_AUTH), Boolean.class, false);
    int connectionTimeout = environment.getProperty(String.format("%s.%s", ENV_SPRING_MAIL, PROP_CONNECTION_TIMEOUT), Integer.class, DEFAULT_CONNECTION_TIMEOUT);
    int timeout = environment.getProperty(String.format("%s.%s", ENV_SPRING_MAIL, PROP_TIMEOUT), Integer.class, DEFAULT_TIMEOUT);

    String prefix = mailPropertyPrefix(protocol);

    // https://docs.spring.io/spring-boot/docs/2.7.8/reference/html/application-properties.html#application-properties.mail.spring.mail.properties
    // falls back on the smtp one, the only one supported by previous versions
    String mailProtocols = environment.getProperty(String.format("%s.properties.%s%s", ENV_SPRING_MAIL, prefix, MAIL_SSL_PROTOCOLS),
      environment.getProperty(String.format("%s.properties.mail.smtp.%s", ENV_SPRING_MAIL, MAIL_SSL_PROTOCOLS), DEFAULT_PROP_MAIL_PROTOCOLS));

    Properties sendProperties = new Properties();
    sendProperties.setProperty(PROP_TRANSPORT_PROTO, protocol);
    sendProperties.setProperty(prefix + MAIL_STARTTLS, tls.toString());
    // implicit SSL (usually port 465), always on with smtps
    sendProperties.setProperty(prefix + MAIL_SSL_ENABLE, ssl.toString());
    sendProperties.setProperty(prefix + MAIL_SSL_PROTOCOLS, mailProtocols);
    sendProperties.setProperty(prefix + MAIL_AUTH, auth.toString());
    // avoid blocking the mail thread forever on an unresponsive server
    sendProperties.setProperty(prefix + MAIL_CONNECTION_TIMEOUT, String.valueOf(connectionTimeout));
    sendProperties.setProperty(prefix + MAIL_TIMEOUT, String.valueOf(timeout));
    sendProperties.setProperty(prefix + MAIL_WRITE_TIMEOUT, String.valueOf(timeout));
    return sendProperties;
  }

  private static String mailPropertyPrefix(String protocol) {
    return String.format("mail.%s.", protocol);
  }

  /**
   * Configure OAuth2 authentication for JavaMailSender
   */
  private void configureOAuth2(JavaMailSenderImpl sender, Properties props, String prefix) {
    // Get OAuth2 access token
    String accessToken = oauth2TokenService.getAccessToken();

    // Set OAuth2 token as password
    sender.setPassword(accessToken);

    // Configure XOAUTH2 SASL mechanism
    props.setProperty(prefix + MAIL_AUTH, "true");
    props.setProperty(prefix + MAIL_AUTH_MECHANISMS, "XOAUTH2");

    log.debug("OAuth2 authentication configured with XOAUTH2 SASL mechanism");
  }
}
