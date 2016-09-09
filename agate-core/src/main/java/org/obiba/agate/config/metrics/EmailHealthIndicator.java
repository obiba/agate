/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.config.metrics;

import javax.inject.Inject;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * SpringBoot Actuator HealthIndicator check for JavaMail.
 */
public class EmailHealthIndicator extends HealthCheckIndicator {

  private static final Logger log = LoggerFactory.getLogger(EmailHealthIndicator.class);

  @Inject
  private JavaMailSenderImpl javaMailSender;

  @Override
  protected Result check() {
    try {
      log.debug("Initializing JavaMail health indicator");
      javaMailSender.getSession().getTransport()
        .connect(javaMailSender.getHost(), javaMailSender.getUsername(), javaMailSender.getPassword());

      return healthy();
    } catch(MessagingException e) {
      log.debug("Cannot connect to e-mail server.", e);
      return unhealthy("Cannot connect to e-mail server.", e);
    }
  }
}
