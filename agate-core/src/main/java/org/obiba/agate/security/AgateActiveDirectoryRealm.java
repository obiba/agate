package org.obiba.agate.security;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.agate.domain.ActiveDirectoryRealmConfig;

/**
 * Active Directory realm, with OTP support.
 */
public class AgateActiveDirectoryRealm extends ActiveDirectoryRealm {

  private final AgateRealmHelper helper;

  public AgateActiveDirectoryRealm(String name, ActiveDirectoryRealmConfig activeDirectoryRealmConfig, JndiLdapContextFactory ldapContextFactory, AgateRealmHelper helper) {
    super();
    this.helper = helper;

    setName(name);

    setLdapContextFactory(ldapContextFactory);
    setSearchFilter(activeDirectoryRealmConfig.getSearchFilter());
    setSearchBase(activeDirectoryRealmConfig.getSearchBase());
    setPrincipalSuffix(activeDirectoryRealmConfig.getPrincipalSuffix());
    setPermissionResolver(new AgatePermissionResolver());
    init();
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    AuthenticationInfo authInfo = super.doGetAuthenticationInfo(token);
    if (authInfo != null)
      helper.checkOTP(getName(), token, authInfo);
    return authInfo;
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    return helper.getUserFromAvailablePrincipal(principals, principals.fromRealm(getName()));
  }
}
