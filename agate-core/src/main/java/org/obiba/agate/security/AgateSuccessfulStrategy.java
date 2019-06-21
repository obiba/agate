package org.obiba.agate.security;

import java.util.Collection;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.AbstractAuthenticationStrategy;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.agate.service.UserService;

public class AgateSuccessfulStrategy extends AbstractAuthenticationStrategy {

  private final UserService userService;

  private final RealmConfigService realmConfigService;

  public AgateSuccessfulStrategy(
    UserService userService,
    RealmConfigService realmConfigService) {
    this.userService = userService;
    this.realmConfigService = realmConfigService;
  }

  @Override
  public AuthenticationInfo beforeAllAttempts(Collection<? extends Realm> realms, AuthenticationToken token) throws AuthenticationException {
    return null;
  }

  @Override
  protected AuthenticationInfo merge(AuthenticationInfo info, AuthenticationInfo aggregate) {
    if (aggregate != null && isEmpty(aggregate.getPrincipals())) {
      return aggregate;
    }

    return info != null ? info : aggregate;
  }

  @Override
  public AuthenticationInfo afterAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo singleRealmInfo, AuthenticationInfo aggregateInfo, Throwable t) throws AuthenticationException {
    AuthenticationInfo info;
    if (singleRealmInfo == null) {
      info = aggregateInfo;
    } else {
      if (aggregateInfo == null) {
        info = singleRealmInfo;
      } else {
        info = merge(singleRealmInfo, aggregateInfo);
      }
    }

    if (info != null && info.getPrincipals().fromRealm(realm.getName()).size() > 0 && realmConfigService.findAll().stream().map(RealmConfig::getName).collect(Collectors.toList()).contains(realm.getName())) {
      String username = info.getPrincipals().getPrimaryPrincipal().toString();
      if (Strings.isNullOrEmpty(username)) username = info.getPrincipals().toString();

      User user = userService.findUser(username);
      if (user == null) throw new RuntimeException("User [" + username + "] does not exist.");

      if (!user.getRealm().equals(realm.getName())) {
        info = null;
      }
    }

    return info;
  }

  private boolean isEmpty(PrincipalCollection principals) {
    return principals == null || principals.isEmpty();
  }
}
