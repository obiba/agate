package org.obiba.agate.web.filter.auth.oidc;

import javax.annotation.PostConstruct;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.oidc.OIDCSessionManager;
import org.springframework.stereotype.Component;

@Component("agateSignUpFilter")
public class AgateSignUpFilter extends AbstractAgateAuthenticationFilter {

  private String publicUrl;

  public AgateSignUpFilter(ConfigurationService configurationService,
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
    return FilterAction.SIGNUP.name();
  }
}
