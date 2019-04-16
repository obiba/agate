package org.obiba.agate.security;

import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AgateIniRealm extends IniRealm {

  public static final String INI_REALM = "agate-ini-realm";

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
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    SimpleAccount account = (SimpleAccount) super.doGetAuthorizationInfo(principals);
    // implicitly, give the role agate-user to all users from ini
    if(account != null) account.addRole(Roles.AGATE_USER.toString());

    return account;
  }
}
