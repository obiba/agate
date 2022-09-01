package org.obiba.agate.web.filter.auth.oidc;

import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.oidc.OIDCSessionManager;

public class AgateSignUpFilter extends AbstractAgateAuthenticationFilter {

  private String publicUrl;

  public AgateSignUpFilter(ConfigurationService configurationService,
                           OIDCConfigurationProvider oidcConfigurationProvider,
                           OIDCSessionManager oidcSessionManager,
                           RealmConfigService realmConfigService) {
    super(configurationService, oidcConfigurationProvider, oidcSessionManager, realmConfigService);
    init();
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
    return FilterAction.SIGNUP.name();
  }
}
