package org.obiba.agate.security;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import javax.sql.DataSource;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.realm.ldap.DefaultLdapRealm;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.CollectionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;

@Component
public class AgateRealmFactory {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final UserService userService;

  private final ConfigurationService configurationService;

  private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
  private static final String MARIA_DB_DRIVER = "org.mariadb.jdbc.Driver";
  private static final String POSTGRES_DRIVER = "org.postgresql.Driver";

  @Inject
  public AgateRealmFactory(
    UserService userService,
    ConfigurationService configurationService) {
    this.userService = userService;
    this.configurationService = configurationService;
  }

  public AuthorizingRealm build(RealmConfig realmConfig) {
    if (realmConfig == null || Strings.isNullOrEmpty(realmConfig.getName()) || Strings.isNullOrEmpty(realmConfig.getContent())) throw new RuntimeException("No valid realm configuration");

    AuthorizingRealm realm;

    try {
      JSONObject decryptedContent = new JSONObject(configurationService.decrypt(realmConfig.getContent()));

      switch (realmConfig.getType()) {
        case AGATE_LDAP_REALM:
          DefaultLdapRealm ldapRealm = new DefaultLdapRealm() {
            @Override
            protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
              return getUserFromAvailablePrincipal(principals, principals.fromRealm(getName()));
            }
          };

          JndiLdapContextFactory jndiLdapContextFactory = createLdapContextFactory(realmConfig.getName(), decryptedContent);

          if (jndiLdapContextFactory == null) throw new RuntimeException("Realm not configurable");

          ldapRealm.setName(realmConfig.getName());
          ldapRealm.setContextFactory(jndiLdapContextFactory);
          ldapRealm.setUserDnTemplate(decryptedContent.optString("userDnTemplate"));
          ldapRealm.setPermissionResolver(new AgatePermissionResolver());
          ldapRealm.init();

          realm = ldapRealm;
          break;
        case AGATE_JDBC_REALM:
          JdbcRealm jdbcRealm = new JdbcRealm() {
            @Override
            protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
              return getUserFromAvailablePrincipal(principals, principals.fromRealm(getName()));
            }
          };

          DataSource dataSource = createDataSource(realmConfig.getName(), decryptedContent);

          if (dataSource == null) throw new RuntimeException("Realm not configurable");

          jdbcRealm.setName(realmConfig.getName());
          jdbcRealm.setDataSource(dataSource);
          jdbcRealm.setAuthenticationQuery(decryptedContent.optString("authenticationQuery"));
          jdbcRealm.setPermissionsLookupEnabled(false);

          realm = jdbcRealm;
          break;
        default:
          throw new RuntimeException("Realm not configurable");
      }
    } catch (JSONException e) {
      throw new RuntimeException("Realm not configurable");
    }

    return realm;
  }

  private JndiLdapContextFactory createLdapContextFactory(String configName, JSONObject content) {
    JndiLdapContextFactory jndiLdapContextFactory = new JndiLdapContextFactory();

    String url = content.optString("url");

    if (Strings.isNullOrEmpty(url)) {
      logger.error("Validation failed for {}; No url", configName);
      return null;
    }

    jndiLdapContextFactory.setUrl(url);
    jndiLdapContextFactory.setSystemUsername(content.optString("systemUsername"));
    jndiLdapContextFactory.setSystemPassword(content.optString("systemPassword"));

    return jndiLdapContextFactory;
  }

  private DataSource createDataSource(String configName, JSONObject content) {
    DataSourceBuilder builder = DataSourceBuilder.create();

    String url = content.optString("url");
    String driverClassName = getValidDriverClassName(configName, url);

    if (Strings.isNullOrEmpty(url)) {
      logger.error("Validation failed for {}; No url", configName);
      return null;
    } else if (Strings.isNullOrEmpty(driverClassName)) {
      logger.error("Validation failed for {}; No valid driver class name based on given url", configName);
      return null;
    }

    builder
      .url(url)
      .username(content.optString("username"))
      .password(content.optString("password"))
      .driverClassName(driverClassName);

    return builder.build();
  }

  private String getValidDriverClassName(String configName, String url) {

    if (url.startsWith("jdbc:mysql://")) {
      return MYSQL_DRIVER;
    } else if (url.startsWith("jdbc:mariadb://")) {
      return MARIA_DB_DRIVER;
    } else if (url.startsWith("jdbc:postgresql://")) {
      return POSTGRES_DRIVER;
    } else {
      logger.error("Validation failed for {}; No valid driver class name based on given url", configName);
      return null;
    }
  }

  private SimpleAuthorizationInfo getUserFromAvailablePrincipal(PrincipalCollection principals, Collection principalsFromRealm) {
    if(principalsFromRealm != null && !principalsFromRealm.isEmpty()) {
      User user = userService.findUser((String) (!CollectionUtils.isEmpty(principalsFromRealm) ? principalsFromRealm.iterator().next() : principals.getPrimaryPrincipal()));
      return new SimpleAuthorizationInfo(user == null
        ? Collections.emptySet()
        : ImmutableSet.<String>builder().add(user.getRole(), Roles.AGATE_USER.toString()).build());
    }

    return new SimpleAuthorizationInfo(ImmutableSet.of(Roles.AGATE_USER.toString()));
  }

}
