/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.security;

import com.google.common.base.Strings;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.NoSuchUserException;
import org.obiba.agate.service.TotpService;
import org.obiba.agate.service.UserService;
import org.obiba.shiro.NoSuchOtpException;
import org.obiba.shiro.authc.TicketAuthenticationToken;
import org.obiba.shiro.web.filter.AbstractAuthenticationExecutor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;

@Component
public class AuthenticationExecutorImpl extends AbstractAuthenticationExecutor implements InitializingBean {

  @Value("${login.maxTry:3}")
  private int maxTry;

  @Value("${login.trialTime:300}")
  private int trialTime;

  @Value("${login.banTime:300}")
  private int banTime;

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private UserService userService;

  private TotpService totpService;

  @Override
  public void afterPropertiesSet() {
    configureBan(maxTry, trialTime, banTime);
  }

  @Override
  protected void ensureProfile(Subject subject) {
    // do nothing
  }

  @Override
  protected void processRequest(HttpServletRequest request, AuthenticationToken token) {
    if (!(token instanceof TicketAuthenticationToken) && token.getPrincipal() instanceof String && token.getCredentials() != null) {
      Configuration config = configurationService.getConfiguration();
      if (config.hasOtpStrategy()) {
        String otpHeader = request.getHeader("X-Obiba-" + config.getOtpStrategy());
        validateOtp(config.getOtpStrategy(), otpHeader, token);
      }
    }
    super.processRequest(request, token);
  }


  private void validateOtp(String strategy, String code, AuthenticationToken token) {
    String username = token.getPrincipal().toString();
    try {
      User user = userService.findActiveUser(username);
      if(user == null) user = userService.findActiveUserByEmail(username);
      if (user != null && (user.hasSecret() || configurationService.getConfiguration().isEnforced2FA())) {
        if (Strings.isNullOrEmpty(code)) {
          throw new NoSuchOtpException("X-Obiba-" + strategy);
        }
        if (user.hasSecret()) {
          if (!totpService.validateCode(code, user.getSecret()))
            throw new AuthenticationException("Wrong TOTP");
        } else if (user.hasOtp()) {
          if (!userService.validateOtp(user, code))
            throw new AuthenticationException("Wrong TOTP");
        }
      } // else 2FA not activated
    } catch (NoSuchUserException e) {
      // first login or wrong username
    }
  }
}
