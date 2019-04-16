package org.obiba.agate.security;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.ldap.DefaultLdapRealm;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.UserService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;

@Component
public class AgateRealmFactory {

  private final UserService userService;

  private final ConfigurationService configurationService;

  @Inject
  public AgateRealmFactory(
    UserService userService,
    ConfigurationService configurationService) {
    this.userService = userService;
    this.configurationService = configurationService;
  }

  public AuthorizingRealm build(RealmConfig realmConfig) {
    if (realmConfig == null || Strings.isNullOrEmpty(realmConfig.getName())/* || Strings.isNullOrEmpty(realmConfig.getContent())*/) throw new RuntimeException("No valid realm configuration");

    AuthorizingRealm realm;

//    try {
    switch (realmConfig.getRealm()) {
      case AGATE_LDAP_REALM:
          realm = new DefaultLdapRealm() {
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
          };

        JndiLdapContextFactory jndiLdapContextFactory = new JndiLdapContextFactory();
        jndiLdapContextFactory.setUrl("ldap://172.17.127.161:389");
        jndiLdapContextFactory.setSystemUsername("admin");
        jndiLdapContextFactory.setSystemPassword("password");

        ((DefaultLdapRealm) realm).setContextFactory(jndiLdapContextFactory);
        ((DefaultLdapRealm) realm).setUserDnTemplate("uid={0},ou=People,dc=brandy,dc=com");
        realm.setPermissionResolver(new AgatePermissionResolver());
//        realm.setCredentialsMatcher(new LdapShaPasswordEncoder());
        realm.init();
        break;
      default:
        throw new RuntimeException("Realm not configurable");
    }
//    } catch (JSONException e) {
//      throw new RuntimeException("Realm not configurable");
//    }

    return realm;
  }

}
