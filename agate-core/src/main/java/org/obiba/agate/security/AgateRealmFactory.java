package org.obiba.agate.security;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
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
import org.obiba.agate.service.TotpService;
import org.obiba.agate.service.UserService;
import org.obiba.oidc.shiro.realm.OIDCRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    AgateRealmHelper helper = new AgateRealmHelper(configurationService, userService);

    AuthorizingRealm realm;

    try {

      switch (realmConfig.getType()) {

        case AGATE_LDAP_REALM:
          LdapRealmConfig ldapConfig = LdapRealmConfig.newBuilder(configurationService.decrypt(realmConfig.getContent())).build();
          JndiLdapContextFactory jndiLdapContextFactory = createLdapContextFactory(realmConfig.getName(), ldapConfig.getUrl(), ldapConfig.getSystemUsername(), ldapConfig.getSystemPassword());
          if (jndiLdapContextFactory == null) throw new RuntimeException("Realm [" + realmConfig.getName() + "] not configurable");
          realm = new AgateLdapRealm(realmConfig.getName(), ldapConfig, jndiLdapContextFactory, helper);
          break;
        case AGATE_JDBC_REALM:
          JdbcRealmConfig jdbcConfig = JdbcRealmConfig.newBuilder(configurationService.decrypt(realmConfig.getContent())).build();
          DataSource dataSource = createDataSource(realmConfig.getName(), jdbcConfig);
          if (dataSource == null) throw new RuntimeException("Realm [" + realmConfig.getName() + "] not configurable");
          realm = new AgateJdbcRealm(realmConfig.getName(), jdbcConfig, dataSource, helper);
          break;
        case AGATE_AD_REALM:
          ActiveDirectoryRealmConfig activeDirectoryRealmConfig = ActiveDirectoryRealmConfig.newBuilder(configurationService.decrypt(realmConfig.getContent())).build();
          String username = activeDirectoryRealmConfig.getSystemUsername();
          if (!Strings.isNullOrEmpty(activeDirectoryRealmConfig.getPrincipalSuffix()))
            username = username + activeDirectoryRealmConfig.getPrincipalSuffix();
          JndiLdapContextFactory ldapContextFactory = createLdapContextFactory(realmConfig.getName(), activeDirectoryRealmConfig.getUrl(),
              username, activeDirectoryRealmConfig.getSystemPassword());
          realm = new AgateActiveDirectoryRealm(realmConfig.getName(), activeDirectoryRealmConfig, ldapContextFactory, helper);
          break;

        case AGATE_OIDC_REALM:
          OidcRealmConfig oidcRealmConfig = OidcRealmConfig.newBuilder(configurationService.decrypt(realmConfig.getContent()))
              .setUserInfoMapping(realmConfig.getUserInfoMapping())
              .build();
          realm = new AgateOidcRealm(realmConfig.getName(), oidcRealmConfig, helper);
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
      String principal = (String) (!CollectionUtils.isEmpty(principalsFromRealm) ? principalsFromRealm.iterator().next() : principals.getPrimaryPrincipal());

      User user = userService.findUser(principal);
      if (user == null) {
        user = userService.findActiveUserByEmail(principal);
      }

      return new SimpleAuthorizationInfo(user == null
        ? Collections.emptySet()
        : ImmutableSet.<String>builder().add(user.getRole(), Roles.AGATE_USER.toString()).build());
    }

    return new SimpleAuthorizationInfo(ImmutableSet.of(Roles.AGATE_USER.toString()));
  }

}
