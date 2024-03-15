package org.obiba.agate.security;

import com.google.common.base.Strings;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.agate.domain.JdbcRealmConfig;

import javax.sql.DataSource;

/**
 * SQL database realm, with OTP support.
 */
public class AgateJdbcRealm extends JdbcRealm {

  private final JdbcRealmConfig jdbcConfig;

  private final AgateRealmHelper helper;

  public AgateJdbcRealm(String name, JdbcRealmConfig jdbcConfig, DataSource dataSource, AgateRealmHelper helper) {
    super();
    this.jdbcConfig = jdbcConfig;
    this.helper = helper;
    setName(name);
    setDataSource(dataSource);
    setAuthenticationQuery(jdbcConfig.getAuthenticationQuery());
    setSaltStyle(jdbcConfig.getSaltStyle());
    if (jdbcConfig.getSaltStyle() != SaltStyle.NO_SALT) {
      HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
      hashedCredentialsMatcher.setHashAlgorithmName(jdbcConfig.getAlgorithmName());

      setCredentialsMatcher(hashedCredentialsMatcher);
    }
    setPermissionsLookupEnabled(false);
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

  @Override
  protected String getSaltForUser(String username) {
    String externalSalt = jdbcConfig.getExternalSalt();
    return Strings.isNullOrEmpty(externalSalt) ? super.getSaltForUser(username) : externalSalt;
  }
}
