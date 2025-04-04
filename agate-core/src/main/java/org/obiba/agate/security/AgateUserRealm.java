/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.agate.security;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.SimpleByteSource;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserCredentials;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.TotpService;
import org.obiba.agate.service.UserService;
import org.obiba.shiro.NoSuchOtpException;
import org.obiba.shiro.authc.UsernamePasswordOtpToken;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import java.util.Collection;
import java.util.Collections;

/**
 * Realm for users defined in opal's own users database.
 */
@Component
public class AgateUserRealm extends AuthorizingRealm implements InitializingBean {

//  public static final String AGATE_REALM = "agate-user-realm";

  @Inject
  private UserService userService;

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private TotpService totpService;

  @Inject
  private Environment env;

  /**
   * Number of times the user password is hashed for attack resiliency
   */
  private int nbHashIterations;

  private String salt;

  @Override
  public void afterPropertiesSet() {
    setCacheManager(new MemoryConstrainedCacheManager());

    nbHashIterations = env.getProperty("shiro.password.nbHashIterations", Integer.class, 10000);

    HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher(Sha512Hash.ALGORITHM_NAME);
    credentialsMatcher.setHashIterations(nbHashIterations);
    setCredentialsMatcher(credentialsMatcher);

    salt = env.getProperty("shiro.password.salt");
  }

  @Override
  public String getName() {
    return AgateRealm.AGATE_USER_REALM.getName();
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    UsernamePasswordToken upToken = (UsernamePasswordToken) token;
    String username = upToken.getUsername();

    // Null username is invalid
    if(username == null) {
      throw new AccountException("Null usernames are not allowed by this realm.");
    }

    User user = userService.findActiveUser(username);
    if(user == null) user = userService.findActiveUserByEmail(username);
    if(user == null || !user.isEnabled() || !user.getRealm().equals(AgateRealm.AGATE_USER_REALM.getName()))
      throw new UnknownAccountException("No account found for user [" + username + "]");

    username = user.getName();
    UserCredentials userCredentials = userService.findUserCredentials(username);
    if(userCredentials == null) throw new UnknownAccountException("No account found for user [" + username + "]");

    if (user.hasSecret() || configurationService.getConfiguration().isEnforced2FA()) {
      String strategy = configurationService.getConfiguration().getOtpStrategy();
      if (strategy.equals("TOTP")) {
        String code = token instanceof UsernamePasswordOtpToken ? ((UsernamePasswordOtpToken) token).getOtp() : null;
        if (Strings.isNullOrEmpty(code)) throw new NoSuchOtpException("X-Obiba-" + strategy);
        if (user.hasSecret()) {
          if (!totpService.validateCode(code, user.getSecret()))
            throw new AuthenticationException("Wrong TOTP");
        } else if (user.hasTempSecret()) {
          if (totpService.validateCode(code, user.getTempSecret())) {
            // confirm secret
            user.confirmSecret();
            userService.save(user);
          } else {
            // reset failing temp secret
            user.resetSecret(null);
            userService.save(user);
            throw new AuthenticationException("Wrong TOTP");
          }
        } else if (user.hasOtp()) {
          if (!userService.validateOtp(user, code))
            throw new AuthenticationException("Wrong TOTP");
        }
      }
    }

    SimpleAuthenticationInfo authInfo = new SimpleAuthenticationInfo(username, userCredentials.getPassword(), getName());
    authInfo.setCredentialsSalt(new SimpleByteSource(salt));
    return authInfo;
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    Collection<?> thisPrincipals = principals.fromRealm(getName());
    if(thisPrincipals != null && !thisPrincipals.isEmpty()) {
      Object primary = thisPrincipals.iterator().next();
      PrincipalCollection simplePrincipals = new SimplePrincipalCollection(primary, getName());
      String username = (String) getAvailablePrincipal(simplePrincipals);
      User user = userService.findActiveUser(username);
      return new SimpleAuthorizationInfo(user == null
          ? Collections.emptySet()
          : ImmutableSet.<String>builder().add(user.getRole(), Roles.AGATE_USER.toString()).build()); //adding agate-user role implicitly.
    }
    return new SimpleAuthorizationInfo();

  }

}
