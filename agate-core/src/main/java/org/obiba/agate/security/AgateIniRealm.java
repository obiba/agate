package org.obiba.agate.security;

import com.google.common.base.Strings;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.TotpService;
import org.obiba.shiro.NoSuchOtpException;
import org.obiba.shiro.authc.UsernamePasswordOtpToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AgateIniRealm extends IniRealm {

  private final Logger logger = LoggerFactory.getLogger(AgateIniRealm.class);

  public static final String INI_REALM = "agate-ini-realm";

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private TotpService totpService;

  @Override
  public String getName() {
    return INI_REALM;
  }

  public AgateIniRealm() {
    super("classpath:shiro.ini");
    this.setPermissionResolver(new AgatePermissionResolver());
    this.setCredentialsMatcher(new PasswordMatcher());
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    AuthenticationInfo authInfo = super.doGetAuthenticationInfo(token);

    // check for administrator secret, 2FA code validation etc.
    if (authInfo != null && getConfiguration().hasSecretOtp()) {
      String code = token instanceof UsernamePasswordOtpToken ? ((UsernamePasswordOtpToken) token).getOtp() : null;
      if (Strings.isNullOrEmpty(code)) throw new NoSuchOtpException("X-Obiba-" + getConfiguration().getOtpStrategy());
      if (!totpService.validateCode(code, getConfiguration().getSecretOtp()))
        throw new AuthenticationException("Wrong TOTP");
    }

    return authInfo;
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    SimpleAccount account = (SimpleAccount) super.doGetAuthorizationInfo(principals);
    // implicitly, give the role agate-user to all users from ini
    if(account != null) account.addRole(Roles.AGATE_USER.toString());

    return account;
  }

  private Configuration getConfiguration() {
    return configurationService.getConfiguration();
  }
}
