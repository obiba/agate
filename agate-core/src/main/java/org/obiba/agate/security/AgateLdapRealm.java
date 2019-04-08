package org.obiba.agate.security;

import com.google.common.collect.ImmutableSet;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.UserService;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;

@Component
public class AgateLdapRealm extends AuthorizingRealm {
  public static final String AGATE_LDAP_REALM = "agate-ldap-realm";

  private LdapTemplate ldapTemplate;

  @PostConstruct
  public void configure() {
    LdapContextSource ldapContextSource = new LdapContextSource();
    ldapContextSource.setUrl("ldap://172.17.127.169:389");
    ldapContextSource.setBase("dc=brandy,dc=com");
    ldapContextSource.setUserDn("cn=admin,dc=brandy,dc=com");
    ldapContextSource.setPassword("password");
    ldapContextSource.afterPropertiesSet();
    ldapTemplate = new LdapTemplate(ldapContextSource);
  }

  @Inject
  private UserService userService;

  @Inject
  private Environment env;


  @Override
  public String getName() {
    return AGATE_LDAP_REALM;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    UsernamePasswordToken upToken = (UsernamePasswordToken) token;
    String username = upToken.getUsername();
    return new SimpleAuthenticationInfo(username, "", getName());
  }

  @Override
  protected void assertCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) throws AuthenticationException {
    UsernamePasswordToken upToken = (UsernamePasswordToken) token;
    String username = upToken.getUsername();
    LdapQuery criteria = LdapQueryBuilder.query().where("uid").is(username);
    ldapTemplate.authenticate(criteria, new String(upToken.getPassword()));
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    Collection<?> thisPrincipals = principals.fromRealm(getName());
    if(thisPrincipals != null && !thisPrincipals.isEmpty()) {
      Object primary = thisPrincipals.iterator().next();
      PrincipalCollection simplePrincipals = new SimplePrincipalCollection(primary, getName());
      String username = (String) getAvailablePrincipal(simplePrincipals);
      User user = userService.findUser(username);
      return new SimpleAuthorizationInfo(user == null
        ? Collections.emptySet()
        : ImmutableSet.<String>builder().add(user.getRole(), Roles.AGATE_USER.toString()).build()); //adding agate-user role implicitly.
    }

    return new SimpleAuthorizationInfo();
  }

}
