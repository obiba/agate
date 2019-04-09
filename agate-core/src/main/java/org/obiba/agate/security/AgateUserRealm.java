/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.agate.security;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
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
import org.obiba.agate.service.UserService;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

/**
 * Realm for users defined in opal's own users database.
 */
@Component
public class AgateUserRealm extends AuthorizingRealm {

//  public static final String AGATE_REALM = "agate-user-realm";

  @Inject
  private UserService userService;

  @Inject
  private Environment env;

  /**
   * Number of times the user password is hashed for attack resiliency
   */
  private int nbHashIterations;

  private String salt;

  @PostConstruct
  public void postConstruct() {

    setCacheManager(new MemoryConstrainedCacheManager());

    RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "shiro.password.");
    nbHashIterations = propertyResolver.getProperty("nbHashIterations", Integer.class);

    HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher(Sha512Hash.ALGORITHM_NAME);
    credentialsMatcher.setHashIterations(nbHashIterations);
    setCredentialsMatcher(credentialsMatcher);

    salt = propertyResolver.getProperty("salt");
  }

  @Override
  public String getName() {
    return AgateRealm.USER_REALM.getName();
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
    if(user == null || !user.isEnabled() || !user.getRealm().equals(AgateRealm.USER_REALM))
      throw new UnknownAccountException("No account found for user [" + username + "]");

    username = user.getName();
    UserCredentials userCredentials = userService.findUserCredentials(username);
    if(userCredentials == null) throw new UnknownAccountException("No account found for user [" + username + "]");

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
