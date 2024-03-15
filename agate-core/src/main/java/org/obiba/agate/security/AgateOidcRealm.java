package org.obiba.agate.security;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.agate.domain.OidcRealmConfig;
import org.obiba.oidc.shiro.realm.OIDCRealm;

/**
 * OpenID Connect realm, without support for OTP in Agate (feature to be provided by the ID provider).
 */
public class AgateOidcRealm extends OIDCRealm {

  private final AgateRealmHelper helper;

  public AgateOidcRealm(String name, OidcRealmConfig oidcRealmConfig, AgateRealmHelper helper) {
    super(oidcRealmConfig);
    setName(name);
    this.helper = helper;
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    return helper.getUserFromAvailablePrincipal(principals, principals.fromRealm(getName()));
  }
}
