package org.obiba.agate.security;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.CollectionUtils;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserCredentials;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.TotpService;
import org.obiba.agate.service.UserService;
import org.obiba.shiro.NoSuchOtpException;
import org.obiba.shiro.authc.UsernamePasswordOtpToken;

import java.util.Collection;
import java.util.Collections;

public class AgateRealmHelper {

  private final ConfigurationService configurationService;

  private final UserService userService;

  public AgateRealmHelper(ConfigurationService configurationService, UserService userService) {
    this.configurationService = configurationService;
    this.userService = userService;
  }

  public SimpleAuthorizationInfo getUserFromAvailablePrincipal(PrincipalCollection principals, Collection principalsFromRealm) {
    if(principalsFromRealm != null && !principalsFromRealm.isEmpty()) {
      String principal = (String) (!CollectionUtils.isEmpty(principalsFromRealm) ? principalsFromRealm.iterator().next() : principals.getPrimaryPrincipal());

      User user = userService.findUser(principal);
      if (user == null) {
        user = userService.findActiveUserByEmail(principal);
      }

      return new SimpleAuthorizationInfo(user == null
          ? Collections.emptySet()
          : ImmutableSet.<String>builder().add(user.getRole(), Roles.AGATE_USER.toString()).build());
    }

    return new SimpleAuthorizationInfo(ImmutableSet.of(Roles.AGATE_USER.toString()));
  }

  public void checkOTP(String realmName, AuthenticationToken token, AuthenticationInfo authInfo) {
    String username = authInfo.getPrincipals().getPrimaryPrincipal().toString();

    User user = userService.findActiveUser(username);
    if (user == null) user = userService.findActiveUserByEmail(username);
    if (user == null || !user.isEnabled() || !user.getRealm().equals(realmName))
      throw new UnknownAccountException("No account found for user [" + username + "]");

    checkOTP(token, user);
  }

  /**
   * Validate the one-time password of an already password-authenticated user, if 2FA applies.
   */
  public void checkOTP(AuthenticationToken token, User user) {
    if (!user.hasSecret() && !configurationService.getConfiguration().isEnforced2FA()) return; // 2FA not activated
    String strategy = configurationService.getConfiguration().getOtpStrategy();
    if (!"TOTP".equals(strategy)) return;

    String code = token instanceof UsernamePasswordOtpToken ? ((UsernamePasswordOtpToken) token).getOtp() : null;
    if (Strings.isNullOrEmpty(code)) throw new NoSuchOtpException("X-Obiba-" + strategy);
    if (user.hasSecret()) {
      if (!userService.validateCode(user, code))
        throw new AuthenticationException("Wrong TOTP");
    } else if (user.hasTempSecret()) {
      if (userService.validateTempCode(user, code)) {
        // confirm secret
        user.confirmSecret();
        userService.save(user);
      } else if (user.hasOtp()) {
        if (!userService.validateOtp(user, code))
          throw new AuthenticationException("Wrong TOTP");
      } else {
        // reset failing temp secret
        user.resetSecret(null);
        userService.save(user);
        throw new AuthenticationException("Wrong TOTP");
      }
    } else if (user.hasOtp()) {
      if (!userService.validateOtp(user, code))
        throw new AuthenticationException("Wrong TOTP");
    } else {
      // 2FA is enforced but there is nothing to verify the code against (e.g. a failing temporary secret was
      // just dropped): challenge again so that a new temporary secret gets issued
      throw new NoSuchOtpException("X-Obiba-" + strategy);
    }
  }
}
