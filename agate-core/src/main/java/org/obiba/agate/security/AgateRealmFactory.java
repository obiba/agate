package org.obiba.agate.security;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.realm.jdbc.JdbcRealm.SaltStyle;
import org.apache.shiro.realm.ldap.DefaultLdapRealm;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.CollectionUtils;
import org.json.JSONException;
import org.obiba.agate.domain.*;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.UserService;
import org.obiba.oidc.shiro.realm.OIDCRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.Collection;
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
    if (realmConfig == null
      || Strings.isNullOrEmpty(realmConfig.getName())
      || Strings.isNullOrEmpty(realmConfig.getContent())) {
      throw new RuntimeException("No valid realm configuration");
    }

    AuthorizingRealm realm;

    try {

      switch (realmConfig.getType()) {

        case AGATE_LDAP_REALM:
          LdapRealmConfig ldapConfig = LdapRealmConfig.newBuilder(configurationService.decrypt(realmConfig.getContent())).build();
          DefaultLdapRealm ldapRealm = new DefaultLdapRealm() {
            @Override
            protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
              return getUserFromAvailablePrincipal(principals, principals.fromRealm(getName()));
            }
          };

          JndiLdapContextFactory jndiLdapContextFactory = createLdapContextFactory(realmConfig.getName(), ldapConfig.getUrl(), ldapConfig.getSystemUsername(), ldapConfig.getSystemPassword());

          if (jndiLdapContextFactory == null) throw new RuntimeException("Realm [" + realmConfig.getName() + "] not configurable");

          ldapRealm.setName(realmConfig.getName());
          ldapRealm.setContextFactory(jndiLdapContextFactory);
          ldapRealm.setUserDnTemplate(ldapConfig.getUserDnTemplate());
          ldapRealm.setPermissionResolver(new AgatePermissionResolver());
          ldapRealm.init();

          realm = ldapRealm;
          break;
        case AGATE_JDBC_REALM:
          JdbcRealmConfig jdbcConfig = JdbcRealmConfig.newBuilder(configurationService.decrypt(realmConfig.getContent())).build();
          JdbcRealm jdbcRealm = new JdbcRealm() {
            @Override
            protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
              return getUserFromAvailablePrincipal(principals, principals.fromRealm(getName()));
            }

            @Override
            protected String getSaltForUser(String username) {
              String externalSalt = jdbcConfig.getExternalSalt();
              return Strings.isNullOrEmpty(externalSalt) ? super.getSaltForUser(username) : externalSalt;
            }
          };

          DataSource dataSource = createDataSource(realmConfig.getName(), jdbcConfig);

          if (dataSource == null) throw new RuntimeException("Realm [" + realmConfig.getName() + "] not configurable");

          jdbcRealm.setName(realmConfig.getName());
          jdbcRealm.setDataSource(dataSource);
          jdbcRealm.setAuthenticationQuery(jdbcConfig.getAuthenticationQuery());
          jdbcRealm.setSaltStyle(jdbcConfig.getSaltStyle());

          if (jdbcConfig.getSaltStyle() != SaltStyle.NO_SALT) {
            HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
            hashedCredentialsMatcher.setHashAlgorithmName(jdbcConfig.getAlgorithmName());

            jdbcRealm.setCredentialsMatcher(hashedCredentialsMatcher);
          }

          jdbcRealm.setPermissionsLookupEnabled(false);

          realm = jdbcRealm;
          break;
        case AGATE_AD_REALM:

          ActiveDirectoryRealmConfig activeDirectoryRealmConfig = ActiveDirectoryRealmConfig.newBuilder(configurationService.decrypt(realmConfig.getContent())).build();
          ActiveDirectoryRealm activeDirectoryRealm = new ActiveDirectoryRealm() {
            @Override
            protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
              return getUserFromAvailablePrincipal(principals, principals.fromRealm(getName()));
            }
          };

          activeDirectoryRealm.setName(realmConfig.getName());

          JndiLdapContextFactory ldapContextFactory = createLdapContextFactory(realmConfig.getName(), activeDirectoryRealmConfig.getUrl(), activeDirectoryRealmConfig.getSystemUsername(), activeDirectoryRealmConfig.getSystemPassword());

          activeDirectoryRealm.setLdapContextFactory(ldapContextFactory);
          activeDirectoryRealm.setSearchFilter(activeDirectoryRealmConfig.getSearchFilter());
          activeDirectoryRealm.setSearchBase(activeDirectoryRealmConfig.getSearchBase());
          activeDirectoryRealm.setPrincipalSuffix(activeDirectoryRealmConfig.getPrincipalSuffix());
          activeDirectoryRealm.setPermissionResolver(new AgatePermissionResolver());
          activeDirectoryRealm.init();

          realm = activeDirectoryRealm;
          break;

        case AGATE_OIDC_REALM:
          OidcRealmConfig oidcRealmConfig = OidcRealmConfig.newBuilder(configurationService.decrypt(realmConfig.getContent())).build();
          OIDCRealm oidcRealm = new OIDCRealm(oidcRealmConfig) {
            @Override
            protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
              return getUserFromAvailablePrincipal(principals, principals.fromRealm(getName()));
            }
          };
          realm = oidcRealm;
          break;

        default:
          throw new RuntimeException("Realm [" + realmConfig.getName() + "] not configurable");
      }
    } catch (JSONException e) {
      throw new RuntimeException("Realm [" + realmConfig.getName() + "] not configurable");
    }

    return realm;
  }

  private JndiLdapContextFactory createLdapContextFactory(String configName, String url, String systemUsername, String systemPassword) {
    JndiLdapContextFactory jndiLdapContextFactory = new JndiLdapContextFactory();

    if (Strings.isNullOrEmpty(url)) {
      logger.error("Validation failed for {}; No url", configName);
      return null;
    }

    jndiLdapContextFactory.setUrl(url);
    jndiLdapContextFactory.setSystemUsername(systemUsername);
    jndiLdapContextFactory.setSystemPassword(systemPassword);

    Map environment = jndiLdapContextFactory.getEnvironment();
    if (environment == null) {
      environment = new HashMap();
    }

    environment.put("com.sun.jndi.ldap.connect.timeout", "1000");
    environment.put("com.sun.jndi.ldap.read.timeout", "1000");

    return jndiLdapContextFactory;
  }

  private DataSource createDataSource(String configName, JdbcRealmConfig content) {
    DataSourceBuilder builder = DataSourceBuilder.create();

    String url = content.getUrl();
    String driverClassName = getValidDriverClassName(configName, url);

    if (Strings.isNullOrEmpty(url)) {
      logger.error("Validation failed for {}; No url", configName);
      return null;
    } else if (Strings.isNullOrEmpty(driverClassName)) {
      logger.error("Validation failed for {}; No valid driver class name based on given url", configName);
      return null;
    }

    builder
      .type(DriverManagerDataSource.class)
      .url(url)
      .username(content.getUsername())
      .password(content.getPassword())
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
