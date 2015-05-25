package org.obiba.agate.service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

/**
 * Service for sending e-mails.
 * <p/>
 * <p>
 * We use the @Async annotation to send e-mails asynchronously.
 * </p>
 */
@Service
public class MailService {

  private static final Logger log = LoggerFactory.getLogger(MailService.class);

  @Inject
  private Environment env;

  @Inject
  private JavaMailSenderImpl javaMailSender;

  /**
   * System default email address that sends the e-mails.
   */
  private String from;

  @PostConstruct
  public void init() {
    from = env.getProperty("spring.mail.from");
    if(Strings.isNullOrEmpty(from)) {
      from = "agate@example.org";
    }
  }

  /**
   * Send an html message.
   *
   * @param to
   * @param subject
   * @param text
   */
  @Async
  public void sendEmail(String to, String subject, String html) {
    MimeMessage message = javaMailSender.createMimeMessage();
    try {
      // use the true flag to indicate you need a multipart message
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      message.setFrom(new InternetAddress(from));
      message.setSubject(subject);
      // use the true flag to indicate the text included is HTML
      helper.setText(html, true);

      javaMailSender.send(message);
      log.debug("Sent e-mail to User '{}'!", to);
    } catch(MailException me) {
      log.warn("E-mail could not be sent to user '{}', exception is: {}", to, me.getMessage());
    } catch(MessagingException e) {
      log.warn("E-mail could not be sent to user '{}', exception is: {}", to, e.getMessage());
    }
  }
}
