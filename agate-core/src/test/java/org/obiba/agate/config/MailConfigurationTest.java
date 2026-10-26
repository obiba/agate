package org.obiba.agate.config;

import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.obiba.agate.service.OAuth2TokenService;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MailConfiguration
 */
class MailConfigurationTest {

  @Mock
  private OAuth2TokenService oauth2TokenService;

  @InjectMocks
  private MailConfiguration mailConfiguration;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testBuildMailProperties_WithLegacyConfig_ShouldKeepPreviousProperties() {
    // typical configuration of previous versions
    MockEnvironment env = new MockEnvironment()
      .withProperty("spring.mail.protocol", "smtp")
      .withProperty("spring.mail.tls", "true")
      .withProperty("spring.mail.auth", "true")
      .withProperty("spring.mail.properties.mail.smtp.ssl.protocols", "TLSv1.2");

    Properties props = MailConfiguration.buildMailProperties(env);

    // previous properties, unchanged, plus the new ssl.enable (JavaMail default) and timeouts
    assertEquals(Map.of(
      "mail.transport.protocol", "smtp",
      "mail.smtp.starttls.enable", "true",
      "mail.smtp.auth", "true",
      "mail.smtp.ssl.protocols", "TLSv1.2",
      "mail.smtp.ssl.enable", "false",
      "mail.smtp.connectiontimeout", "10000",
      "mail.smtp.timeout", "30000",
      "mail.smtp.writetimeout", "30000"), props);
  }

  @Test
  void testJavaMailSender_WithSmtpAuth_ShouldUsePassword() {
    mailConfiguration.setEnvironment(new MockEnvironment()
      .withProperty("spring.mail.host", "mail.example.org")
      .withProperty("spring.mail.port", "587")
      .withProperty("spring.mail.user", "agate")
      .withProperty("spring.mail.password", "secret")
      .withProperty("spring.mail.auth", "true"));

    JavaMailSenderImpl sender = mailConfiguration.javaMailSender();

    assertEquals("mail.example.org", sender.getHost());
    assertEquals(587, sender.getPort());
    assertEquals("agate", sender.getUsername());
    assertEquals("secret", sender.getPassword());
    assertEquals("true", sender.getJavaMailProperties().getProperty("mail.smtp.auth"));
    assertNull(sender.getJavaMailProperties().getProperty("mail.smtp.auth.mechanisms"));
    verifyNoInteractions(oauth2TokenService);
  }

  @Test
  void testJavaMailSender_WithOAuth2_ShouldUseXOAuth2() {
    when(oauth2TokenService.getAccessToken()).thenReturn("access-token");
    mailConfiguration.setEnvironment(new MockEnvironment()
      .withProperty("spring.mail.auth-type", "oauth2")
      .withProperty("spring.mail.user", "agate@example.org"));

    JavaMailSenderImpl sender = mailConfiguration.javaMailSender();

    assertEquals("access-token", sender.getPassword());
    assertEquals("true", sender.getJavaMailProperties().getProperty("mail.smtp.auth"));
    assertEquals("XOAUTH2", sender.getJavaMailProperties().getProperty("mail.smtp.auth.mechanisms"));
    verify(oauth2TokenService).setJavaMailSender(sender);
  }

  @Test
  void testJavaMailSender_WithOAuth2AndSmtps_ShouldUseSmtpsPrefix() {
    when(oauth2TokenService.getAccessToken()).thenReturn("access-token");
    mailConfiguration.setEnvironment(new MockEnvironment()
      .withProperty("spring.mail.auth-type", "oauth2")
      .withProperty("spring.mail.protocol", "smtps"));

    Properties props = mailConfiguration.javaMailSender().getJavaMailProperties();

    assertEquals("true", props.getProperty("mail.smtps.auth"));
    assertEquals("XOAUTH2", props.getProperty("mail.smtps.auth.mechanisms"));
    assertNull(props.getProperty("mail.smtp.auth.mechanisms"));
  }

  @Test
  void testBuildMailProperties_WithDefaults() {
    Properties props = MailConfiguration.buildMailProperties(new MockEnvironment());

    assertEquals("smtp", props.getProperty("mail.transport.protocol"));
    assertEquals("false", props.getProperty("mail.smtp.starttls.enable"));
    assertEquals("false", props.getProperty("mail.smtp.ssl.enable"));
    assertEquals("false", props.getProperty("mail.smtp.auth"));
    assertEquals("TLSv1.2 TLSv1.3", props.getProperty("mail.smtp.ssl.protocols"));
    assertEquals("10000", props.getProperty("mail.smtp.connectiontimeout"));
    assertEquals("30000", props.getProperty("mail.smtp.timeout"));
    assertEquals("30000", props.getProperty("mail.smtp.writetimeout"));
  }

  @Test
  void testBuildMailProperties_WithStartTls() {
    MockEnvironment env = new MockEnvironment()
      .withProperty("spring.mail.tls", "true")
      .withProperty("spring.mail.auth", "true");

    Properties props = MailConfiguration.buildMailProperties(env);

    assertEquals("true", props.getProperty("mail.smtp.starttls.enable"));
    assertEquals("false", props.getProperty("mail.smtp.ssl.enable"));
    assertEquals("true", props.getProperty("mail.smtp.auth"));
  }

  @Test
  void testBuildMailProperties_WithSslEnabled() {
    MockEnvironment env = new MockEnvironment().withProperty("spring.mail.ssl.enabled", "true");

    Properties props = MailConfiguration.buildMailProperties(env);

    assertEquals("true", props.getProperty("mail.smtp.ssl.enable"));
  }

  @Test
  void testBuildMailProperties_WithTimeouts() {
    MockEnvironment env = new MockEnvironment()
      .withProperty("spring.mail.connection-timeout", "5000")
      .withProperty("spring.mail.timeout", "60000");

    Properties props = MailConfiguration.buildMailProperties(env);

    assertEquals("5000", props.getProperty("mail.smtp.connectiontimeout"));
    assertEquals("60000", props.getProperty("mail.smtp.timeout"));
    assertEquals("60000", props.getProperty("mail.smtp.writetimeout"));
  }

  @Test
  void testBuildMailProperties_WithSmtps_ShouldUseSmtpsPrefix() {
    MockEnvironment env = new MockEnvironment()
      .withProperty("spring.mail.protocol", "smtps")
      .withProperty("spring.mail.auth", "true");

    Properties props = MailConfiguration.buildMailProperties(env);

    assertEquals("smtps", props.getProperty("mail.transport.protocol"));
    assertEquals("true", props.getProperty("mail.smtps.auth"));
    assertEquals("TLSv1.2 TLSv1.3", props.getProperty("mail.smtps.ssl.protocols"));
    assertEquals("10000", props.getProperty("mail.smtps.connectiontimeout"));
    assertEquals("30000", props.getProperty("mail.smtps.timeout"));
    assertNull(props.getProperty("mail.smtp.auth"));
    assertNull(props.getProperty("mail.smtp.timeout"));
  }

  @Test
  void testBuildMailProperties_WithSslProtocolsOverride() {
    MockEnvironment env = new MockEnvironment()
      .withProperty("spring.mail.properties.mail.smtp.ssl.protocols", "TLSv1.3");

    Properties props = MailConfiguration.buildMailProperties(env);

    assertEquals("TLSv1.3", props.getProperty("mail.smtp.ssl.protocols"));
  }

  @Test
  void testBuildMailProperties_WithSmtpsAndLegacySslProtocolsOverride_ShouldFallBackOnSmtpOne() {
    MockEnvironment env = new MockEnvironment()
      .withProperty("spring.mail.protocol", "smtps")
      .withProperty("spring.mail.properties.mail.smtp.ssl.protocols", "TLSv1.3");

    Properties props = MailConfiguration.buildMailProperties(env);

    assertEquals("TLSv1.3", props.getProperty("mail.smtps.ssl.protocols"));
  }
}
