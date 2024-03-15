package org.obiba.agate.security;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.ldap.DefaultLdapRealm;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.agate.domain.LdapRealmConfig;

/**
 * LDAP realm, with OTP support.
 */
public class AgateLdapRealm extends DefaultLdapRealm {

  private final AgateRealmHelper helper;

  public AgateLdapRealm(String name, LdapRealmConfig ldapConfig, JndiLdapContextFactory jndiLdapContextFactory, AgateRealmHelper helper) {
    super();
    this.helper = helper;
    setName(name);
    setContextFactory(jndiLdapContextFactory);
    setUserDnTemplate(ldapConfig.getUserDnTemplate());
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
