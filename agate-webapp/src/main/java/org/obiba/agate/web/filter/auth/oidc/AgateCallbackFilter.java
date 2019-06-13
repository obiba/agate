/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.agate.web.filter.auth.oidc;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.TicketService;
import org.obiba.agate.service.TokenUtils;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.oidc.OIDCCredentials;
import org.obiba.oidc.OIDCSession;
import org.obiba.oidc.OIDCSessionManager;
import org.obiba.oidc.shiro.authc.OIDCAuthenticationToken;
import org.obiba.oidc.web.filter.OIDCCallbackFilter;
import org.obiba.shiro.web.filter.AuthenticationExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;

@Component("agateCallbackFilter")
public class AgateCallbackFilter extends OIDCCallbackFilter {

  private static final Logger log = LoggerFactory.getLogger(AgateCallbackFilter.class);

  private final ConfigurationService configurationService;

  private final OIDCConfigurationProvider oidcConfigurationProvider;

  private final OIDCSessionManager oidcSessionManager;

  private final AuthenticationExecutor authenticationExecutor;

  private final TicketService ticketService;

  private final TokenUtils tokenUtils;

  private String publicUrl;

  @Inject
  public AgateCallbackFilter(OIDCConfigurationProvider oidcConfigurationProvider,
                             OIDCSessionManager oidcSessionManager,
                             AuthenticationExecutor authenticationExecutor,
                             ConfigurationService configurationService,
                             TicketService ticketService,
                             TokenUtils tokenUtils) {
    this.oidcConfigurationProvider = oidcConfigurationProvider;
    this.oidcSessionManager = oidcSessionManager;
    this.authenticationExecutor = authenticationExecutor;
    this.configurationService = configurationService;
    this.ticketService = ticketService;
    this.tokenUtils = tokenUtils;
  }

  @PostConstruct
  public void init() {
    publicUrl = configurationService.getPublicUrl();
    setOIDCConfigurationProvider(oidcConfigurationProvider);
    setOIDCSessionManager(oidcSessionManager);
    setDefaultRedirectURL(publicUrl);
    String callbackUrl = publicUrl + (publicUrl.endsWith("/") ? "" : "/") + "auth/callback/";
    setCallbackURL(callbackUrl);
  }

  @Override
  protected void onAuthenticationSuccess(OIDCSession session, OIDCCredentials credentials, HttpServletResponse response) {
    Subject subject = authenticationExecutor.login(new OIDCAuthenticationToken(credentials));
    if (subject != null) {
      Session subjectSession = subject.getSession();
      log.trace("Binding subject {} session {} to executing thread {}", subject.getPrincipal(), subjectSession.getId(), Thread.currentThread().getId());
      ThreadContext.bind(subject);
      subjectSession.touch();
      int timeout = (int) (subjectSession.getTimeout() / 1000);
      response.addHeader(HttpHeaders.SET_COOKIE,
        new NewCookie("agatesid", subjectSession.getId().toString(), "/", null, null, timeout, false).toString());
      log.debug("Successfully authenticated subject {}", SecurityUtils.getSubject().getPrincipal());
    }
  }

  public static class Wrapper extends DelegatingFilterProxy {
    public Wrapper() {
      super("agateCallbackFilter");
    }
  }

}
