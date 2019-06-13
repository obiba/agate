package org.obiba.agate.web.filter.auth.oidc;/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


import org.obiba.agate.service.ConfigurationService;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.oidc.OIDCException;
import org.obiba.oidc.OIDCSessionManager;
import org.obiba.oidc.web.filter.OIDCLoginFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component("agateSignInFilter")
public class AgateSignInFilter extends OIDCLoginFilter {

  private final ConfigurationService configurationService;

  private final OIDCConfigurationProvider oidcConfigurationProvider;

  private final OIDCSessionManager oidcSessionManager;

  private String publicUrl;

  @Inject
  public AgateSignInFilter(ConfigurationService configurationService,
                           OIDCConfigurationProvider oidcConfigurationProvider,
                           OIDCSessionManager oidcSessionManager) {
    this.configurationService = configurationService;
    this.oidcConfigurationProvider = oidcConfigurationProvider;
    this.oidcSessionManager = oidcSessionManager;
  }

  @PostConstruct
  public void init() {
    publicUrl = configurationService.getPublicUrl();
    setOIDCConfigurationProvider(oidcConfigurationProvider);
    setOIDCSessionManager(oidcSessionManager);
    String callbackUrl = publicUrl + (publicUrl.endsWith("/") ? "" : "/") + "auth/callback/";
    setCallbackURL(callbackUrl);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    try {
      super.doFilterInternal(request, response, filterChain);
    } catch (OIDCException e) {
      response.sendRedirect(publicUrl);
    }
  }

  public static class Wrapper extends DelegatingFilterProxy {
    public Wrapper() {
      super("agateSignInFilter");
    }
  }

}
