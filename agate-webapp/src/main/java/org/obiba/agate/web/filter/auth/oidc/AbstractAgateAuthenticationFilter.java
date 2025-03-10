package org.obiba.agate.web.filter.auth.oidc;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.event.AgateConfigUpdatedEvent;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.oidc.OIDCConfigurationProvider;
import org.obiba.oidc.OIDCException;
import org.obiba.oidc.OIDCSession;
import org.obiba.oidc.OIDCSessionManager;
import org.obiba.oidc.utils.OIDCHelper;
import org.obiba.oidc.web.J2EContext;
import org.obiba.oidc.web.filter.OIDCLoginFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public abstract class AbstractAgateAuthenticationFilter extends OIDCLoginFilter {

  private static final Logger log = LoggerFactory.getLogger(AbstractAgateAuthenticationFilter.class);

  private final RealmConfigService realmConfigService;

  private final ConfigurationService configurationService;

  private final OIDCConfigurationProvider oidcConfigurationProvider;

  private final OIDCSessionManager oidcSessionManager;

  public abstract String getPublicUrl();

  public abstract void setPublicUrl(String publicUrl);

  public abstract String getAction();

  public AbstractAgateAuthenticationFilter(
      ConfigurationService configurationService,
      OIDCConfigurationProvider oidcConfigurationProvider,
      OIDCSessionManager oidcSessionManager,
      RealmConfigService realmConfigService) {
    this.configurationService = configurationService;
    this.oidcConfigurationProvider = oidcConfigurationProvider;
    this.oidcSessionManager = oidcSessionManager;
    this.realmConfigService = realmConfigService;
  }

  public void init() {
    setPublicUrl(configurationService.getPublicUrl());
    setOIDCConfigurationProvider(oidcConfigurationProvider);
    setOIDCSessionManager(oidcSessionManager);
    initFilterUrls();
  }

  protected void initFilterUrls() {
    setPublicUrl(configurationService.getPublicUrl());
    String callbackUrl = getPublicUrl() + (getPublicUrl().endsWith("/") ? "" : "/") + "auth/callback/";
    setCallbackURL(callbackUrl);
  }

  @Subscribe
  public void agateConfigUpdated(AgateConfigUpdatedEvent event) {
    initFilterUrls();
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    try {
      super.doFilterInternal(request, response, filterChain);
    } catch (OIDCException e) {
      response.sendRedirect(getPublicUrl());
    }
  }

  protected J2EContext makeJ2EContext(HttpServletRequest request, HttpServletResponse response) {
    String sid = request.getRequestedSessionId();
    log.debug("login filter requested session id: {}", sid);
    return super.makeJ2EContext(request, response);
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
    map.put(FilterParameter.ACTION.value(), new String[]{getAction()});
    map.put(FilterParameter.OIDC_PROVIDER_ID.value(), new String[]{OIDCHelper.extractProviderName(context, getProviderParameter())});

    return new OIDCSession(context.getClientId(), authRequest.getState(), authRequest.getNonce(), Collections.unmodifiableMap(map));
  }

  @Override
  protected String makeCallbackURL(String provider, String callbackURL) {
    RealmConfig realmConfig = realmConfigService.findConfig(provider);
    // get agate's callback url from this realm config, if defined
    if (realmConfig.hasPublicUrl()) {
      String cbURL = realmConfig.getPublicUrl() + "/auth/callback/";
      return super.makeCallbackURL(provider, cbURL);
    }
    // otherwise fallback to agate's public url
    return super.makeCallbackURL(provider, callbackURL);
  }
}
