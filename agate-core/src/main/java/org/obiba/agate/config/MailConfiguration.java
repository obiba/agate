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

  private static final String PROP_AUTH = "auth";

  private static final String PROP_SMTP_AUTH = "mail.smtp.auth";

  private static final String PROP_STARTTLS = "mail.smtp.starttls.enable";

  private static final String PROP_TRANSPORT_PROTO = "mail.transport.protocol";

  private static final String PROP_MAIL_PROTOCOLS = "mail.smtp.ssl.protocols";

  private static final String DEFAULT_PROP_MAIL_PROTOCOLS = "TLSv1.2";

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
    String protocol = environment.getProperty(String.format("%s.%s", ENV_SPRING_MAIL, PROP_PROTO), "smtp");
    Boolean tls = environment.getProperty(String.format("%s.%s", ENV_SPRING_MAIL, PROP_TLS), Boolean.class, false);
    Boolean auth = environment.getProperty(String.format("%s.%s", ENV_SPRING_MAIL, PROP_AUTH), Boolean.class, false);

    // https://docs.spring.io/spring-boot/docs/2.7.8/reference/html/application-properties.html#application-properties.mail.spring.mail.properties
    String mailProtocols = environment.getProperty(String.format("%s.%s", ENV_SPRING_MAIL + ".properties", PROP_MAIL_PROTOCOLS), DEFAULT_PROP_MAIL_PROTOCOLS);

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

    Properties sendProperties = new Properties();
    sendProperties.setProperty(PROP_STARTTLS, tls.toString());
    sendProperties.setProperty(PROP_TRANSPORT_PROTO, protocol);
    sendProperties.setProperty(PROP_MAIL_PROTOCOLS, mailProtocols);

    if ("oauth2".equalsIgnoreCase(authType)) {
      // OAuth2 authentication
      log.info("Configuring OAuth2 authentication for email");
      configureOAuth2(sender, sendProperties);

      // Register JavaMailSender with OAuth2TokenService for automatic token updates
      oauth2TokenService.setJavaMailSender(sender);
    } else {
      // Traditional SMTP authentication (default)
      log.info("Configuring SMTP authentication for email");
      sender.setPassword(password);
      sendProperties.setProperty(PROP_SMTP_AUTH, auth.toString());
    }

    sender.setJavaMailProperties(sendProperties);
    return sender;
  }

  /**
   * Configure OAuth2 authentication for JavaMailSender
   */
  private void configureOAuth2(JavaMailSenderImpl sender, Properties props) {
    // Get OAuth2 access token
    String accessToken = oauth2TokenService.getAccessToken();

    // Set OAuth2 token as password
    sender.setPassword(accessToken);

    // Configure XOAUTH2 SASL mechanism
    props.setProperty("mail.smtp.auth", "true");
    props.setProperty("mail.smtp.auth.mechanisms", "XOAUTH2");

    log.debug("OAuth2 authentication configured with XOAUTH2 SASL mechanism");
  }
}
