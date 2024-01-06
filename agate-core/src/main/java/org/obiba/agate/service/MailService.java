/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Service for sending e-mails. We use the @Async annotation to send e-mails asynchronously.
 */
@Service
public class MailService implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(MailService.class);

  @Inject
  private Environment env;

  @Inject
  private JavaMailSenderImpl javaMailSender;

  /**
   * System default email address that sends the e-mails.
   */
  private String from;

  @Override
  public void afterPropertiesSet() {
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
  public void sendEmail(String to, String subject, String text) {
    MimeMessage message = javaMailSender.createMimeMessage();
    try {
      // use the true flag to indicate you need a multipart message
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setTo(to);
      message.setFrom(new InternetAddress(from));
      message.setSubject(subject);
      // use the true flag to indicate the text included is HTML
      helper.setText(text, true);

      log.trace("Sending e-mail to User '{}'!", to);
      log.trace(text);
      javaMailSender.send(message);
      log.debug("Sent e-mail to User '{}'!", to);
    } catch(MailException | MessagingException me) {
      log.warn("E-mail could not be sent to user '{}', exception is: {}", to, me.getMessage());
    }
  }
}
