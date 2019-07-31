package org.obiba.agate.web.filter.auth.oidc;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.obiba.agate.event.AgateConfigUpdatedEvent;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.oidc.OIDCException;
import org.obiba.oidc.OIDCSession;
import org.obiba.oidc.OIDCSessionManager;
import org.obiba.oidc.utils.OIDCHelper;
import org.obiba.oidc.web.J2EContext;
import org.obiba.oidc.web.filter.OIDCLoginFilter;

public abstract class AbstractAgateAuthenticationFilter extends OIDCLoginFilter {

  private final ConfigurationService configurationService;

  private final OIDCConfigurationProvider oidcConfigurationProvider;

  private final OIDCSessionManager oidcSessionManager;

  public abstract String getPublicUrl();

  public abstract void setPublicUrl(String publicUrl);

  public abstract String getAction();

  public AbstractAgateAuthenticationFilter(
    ConfigurationService configurationService,
    OIDCConfigurationProvider oidcConfigurationProvider,
    OIDCSessionManager oidcSessionManager) {
    this.configurationService = configurationService;
    this.oidcConfigurationProvider = oidcConfigurationProvider;
    this.oidcSessionManager = oidcSessionManager;
  }

  public void init() {
    setPublicUrl(configurationService.getPublicUrl());
    setOIDCConfigurationProvider(oidcConfigurationProvider);
    setOIDCSessionManager(oidcSessionManager);
    String callbackUrl = getPublicUrl() + (getPublicUrl().endsWith("/") ? "" : "/") + "auth/callback/";
    setCallbackURL(callbackUrl);
  }

  @Subscribe
  public void agateConfigUpdated(AgateConfigUpdatedEvent event) {
    setPublicUrl(configurationService.getPublicUrl());
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    try {
      super.doFilterInternal(request, response, filterChain);
    } catch (OIDCException e) {
      response.sendRedirect(getPublicUrl());
    }
  }

  /**
   * Creates an OIDC session and adds the <code>action</code> request parameter to be used later by the callback filter.
   *
   * @param context
   * @param authRequest
   * @return
   */
  @Override
  protected OIDCSession makeSession(J2EContext context, AuthenticationRequest authRequest) {
    Map<String, String[]> requestParameters = context.getRequestParameters();
    Map<String, String[]> map = Maps.newHashMap(requestParameters);
    map.put(FilterParameter.ACTION.value(), new String[] {getAction()});
    map.put(FilterParameter.OIDC_PROVIDER_ID.value(), new String[] {OIDCHelper.extractProviderName(context, getProviderParameter())});

    return new OIDCSession(context.getClientId(), authRequest.getState(), authRequest.getNonce(), Collections.unmodifiableMap(map));
  }
}
