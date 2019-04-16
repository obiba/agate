/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.agate.security;

import java.util.Set;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import net.sf.ehcache.CacheManager;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.authz.permission.PermissionResolverAware;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.obiba.shiro.SessionStorageEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@DependsOn("cacheConfiguration")
public class SecurityManagerFactory implements FactoryBean<SessionsSecurityManager> {

  private static final Logger log = LoggerFactory.getLogger(SecurityManagerFactory.class);

  private static final long SESSION_VALIDATION_INTERVAL = 300000l; // 5 minutes

  private SessionsSecurityManager securityManager;

  private final CacheManager cacheManager;

  private final Set<Realm> realms;

  @Inject
  public SecurityManagerFactory(
    CacheManager cacheManager,
    Set<Realm> realms) {
    this.cacheManager = cacheManager;
    this.realms = realms;
  }

  @Override
  public SessionsSecurityManager getObject() throws Exception {
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

  private SessionsSecurityManager doCreateSecurityManager() {
    DefaultWebSecurityManager manager = new DefaultWebSecurityManager(realms);

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
      ((ModularRealmAuthenticator) dsm.getAuthenticator()).setAuthenticationStrategy(new FirstSuccessfulStrategy());
    }
  }
}
