/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.agate.security;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import net.sf.ehcache.CacheManager;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.authz.permission.PermissionResolverAware;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.domain.RealmStatus;
import org.obiba.agate.event.RealmConfigActivatedOrUpdatedEvent;
import org.obiba.agate.event.RealmConfigDeactivatedEvent;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.agate.service.UserService;
import org.obiba.shiro.SessionStorageEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Persistable;
import org.springframework.stereotype.Component;

@Component
@DependsOn("cacheConfiguration")
public class SecurityManagerFactory implements FactoryBean<SessionsSecurityManager> {

  private static final Logger log = LoggerFactory.getLogger(SecurityManagerFactory.class);

  private static final long SESSION_VALIDATION_INTERVAL = 300000l; // 5 minutes

  private SessionsSecurityManager securityManager;

  private final CacheManager cacheManager;

  private final Set<Realm> realms;

  private final RealmConfigService realmConfigService;

  private final UserService userService;

  private final AgateRealmFactory agateRealmFactory;

  private final EventBus eventBus;

  @Inject
  @Lazy
  public SecurityManagerFactory(
    CacheManager cacheManager,
    Set<Realm> realms,
    RealmConfigService realmConfigService,
    UserService userService,
    AgateRealmFactory agateRealmFactory,
    EventBus eventBus) {
    this.cacheManager = cacheManager;
    this.realms = realms;
    this.realmConfigService = realmConfigService;
    this.userService = userService;
    this.agateRealmFactory = agateRealmFactory;
    this.eventBus = eventBus;
  }

  @Override
  public SessionsSecurityManager getObject() {
    if(securityManager == null) {
      securityManager = doCreateSecurityManager();
      SecurityUtils.setSecurityManager(securityManager);
    }
    return securityManager;
  }

  @Override
  public Class<?> getObjectType() {
    return SessionsSecurityManager.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  @PreDestroy
  public void destroySecurityManager() {
    log.debug("Shutdown SecurityManager");
    // Destroy the security manager.
    SecurityUtils.setSecurityManager(null);
    LifecycleUtils.destroy(securityManager);
    securityManager = null;
  }

  @Subscribe
  public void onRealmConfigActivatedOrUpdatedEvent(RealmConfigActivatedOrUpdatedEvent event) {
    RealmConfig persistable = (RealmConfig) event.getPersistable();
    removeRealm(persistable.getName());
    getObject().getRealms().add(agateRealmFactory.build(persistable));

    log.info("Adding realm '{}' to session manager.", persistable.getName());
  }

  @Subscribe
  public void onRealmConfigDeletedEvent(RealmConfigDeactivatedEvent event) {
    RealmConfig persistable = (RealmConfig) event.getPersistable();
    removeRealm(persistable.getName());

    log.info("Removing realm '{}' from session manager.", persistable.getName());
  }

  private void removeRealm(String name) {
    getObject().getRealms().stream()
      .filter(realm -> realm.getName().equals(name))
      .findFirst().ifPresent(realm -> getObject().getRealms().remove(realm));
  }

  private SessionsSecurityManager doCreateSecurityManager() {
    List<Realm> realmsList = new ArrayList<>();
    realmsList.addAll(realms);

    List<AuthorizingRealm> authorizingRealms =
      realmConfigService.findAllByStatus(RealmStatus.ACTIVE)
        .stream()
        .map(agateRealmFactory::build)
        .collect(Collectors.toList());

    if (authorizingRealms.size() > 0) realmsList.addAll(authorizingRealms);

    DefaultWebSecurityManager manager = new DefaultWebSecurityManager(realmsList);

    initializeCacheManager(manager);
    initializeSessionManager(manager);
    initializeSubjectDAO(manager);
    initializeAuthorizer(manager);
    initializeAuthenticator(manager);

    return manager;
  }

  private void initializeCacheManager(DefaultWebSecurityManager dsm) {
    if(dsm.getCacheManager() == null) {
      EhCacheManager ehCacheManager = new EhCacheManager();
      ehCacheManager.setCacheManager(cacheManager);
      dsm.setCacheManager(ehCacheManager);
    }
  }

  private void initializeSessionManager(DefaultWebSecurityManager dsm) {
    DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
    sessionManager.setSessionDAO(new EnterpriseCacheSessionDAO());
    sessionManager.setSessionValidationInterval(SESSION_VALIDATION_INTERVAL);
    sessionManager.setSessionValidationSchedulerEnabled(true);

    dsm.setSessionManager(sessionManager);
  }

  private void initializeSubjectDAO(DefaultWebSecurityManager dsm) {
    if(dsm.getSubjectDAO() instanceof DefaultSubjectDAO) {
      ((DefaultSubjectDAO) dsm.getSubjectDAO()).setSessionStorageEvaluator(new SessionStorageEvaluator());
    }
  }

  private void initializeAuthorizer(DefaultWebSecurityManager dsm) {
    if(dsm.getAuthorizer() instanceof ModularRealmAuthorizer) {
      ((PermissionResolverAware) dsm.getAuthorizer()).setPermissionResolver(new AgatePermissionResolver());
    }
  }

  private void initializeAuthenticator(DefaultWebSecurityManager dsm) {
    if(dsm.getAuthenticator() instanceof ModularRealmAuthenticator) {
      ((ModularRealmAuthenticator) dsm.getAuthenticator()).setAuthenticationStrategy(new AgateSuccessfulStrategy(userService, realmConfigService));
    }
  }
}
