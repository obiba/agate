package org.obiba.agate.web.filter.auth.oidc;
/*
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
import org.obiba.oidc.OIDCSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * This filter is used to redirect clients to an OIDC authentication provider.
 */
@Component("agateSignInFilter")
public class AgateSignInFilter extends AbstractAgateAuthenticationFilter {

  private static final Logger log = LoggerFactory.getLogger(AgateSignInFilter.class);

  private String publicUrl;

  @Inject
  public AgateSignInFilter(
    ConfigurationService configurationService,
    OIDCConfigurationProvider oidcConfigurationProvider,
    OIDCSessionManager oidcSessionManager) {
    super(configurationService, oidcConfigurationProvider, oidcSessionManager);
  }

  @PostConstruct
  @Override
  public void init() {
    super.init();
  }

  @Override
  public String getPublicUrl() {
    return publicUrl;
  }

  @Override
  public void setPublicUrl(String publicUrl) {
    this.publicUrl = publicUrl;
  }

  @Override
  public String getAction() {
    return FilterAction.SIGNIN.name();
  }

  public static class Wrapper extends DelegatingFilterProxy {
    public Wrapper() {
      super("agateSignInFilter");
    }
  }

}
