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

import com.google.common.collect.ImmutableSet;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserCredentials;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.UserService;
import org.springframework.beans.factory.InitializingBean;
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
  private PasswordHasher passwordHasher;

  @Override
  public void afterPropertiesSet() {
    setCacheManager(new MemoryConstrainedCacheManager());
    setCredentialsMatcher(new UpgradingCredentialsMatcher());
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

    return new SimpleAuthenticationInfo(username, userCredentials.getPassword(), getName());
  }

  /**
   * Verify the password before anything related to the one-time password: the OTP challenge (and the temporary
   * secret it may create) must not be reachable by someone who only knows the user name.
   */
  @Override
  protected void assertCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) throws AuthenticationException {
    super.assertCredentialsMatch(token, info);
    checkOtp(token, info.getPrincipals().getPrimaryPrincipal().toString());
  }

  private void checkOtp(AuthenticationToken token, String username) {
    User user = userService.findActiveUser(username);
    if(user == null) throw new UnknownAccountException("No account found for user [" + username + "]");
    new AgateRealmHelper(configurationService, userService).checkOTP(token, user);
  }

  /**
   * Verify the submitted password against the stored hash and, when the hash was produced by an earlier version,
   * replace it with a current one now that the password is known.
   */
  private class UpgradingCredentialsMatcher implements CredentialsMatcher {

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
      if (!(token.getCredentials() instanceof char[])) return false;
      String password = new String((char[]) token.getCredentials());
      String stored = (String) info.getCredentials();
      if (!passwordHasher.matches(password, stored)) return false;

      if (passwordHasher.isLegacy(stored)) {
        String username = (String) info.getPrincipals().getPrimaryPrincipal();
        UserCredentials userCredentials = userService.findUserCredentials(username);
        if (userCredentials != null) {
          userCredentials.setPassword(passwordHasher.hash(password));
          userService.save(userCredentials);
        }
      }
      return true;
    }
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
