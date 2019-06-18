package org.obiba.agate.oidc;

import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Implements the well-known entry point that provides the settings of OpenID Connect.
 */
@Component
public class OIDCConfigurationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(OIDCConfigurationFilter.class);

  private final TokenUtils tokenUtils;

  private final ConfigurationService configurationService;

  @Inject
  public OIDCConfigurationFilter(TokenUtils tokenUtils, ConfigurationService configurationService) {
    this.tokenUtils = tokenUtils;
    this.configurationService = configurationService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    log.info(request.getRequestURI());
    try {
      JSONObject oidcConfig = getOIDCConfiguration(request);
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      out.print(oidcConfig.toString(2));
      out.flush();
    } catch (JSONException e) {
      log.error("JSON error", e);
      filterChain.doFilter(request, response);
    }
  }

  private JSONObject getOIDCConfiguration(HttpServletRequest request) throws JSONException {
    String baseUrl = getBaseUrl(request);
    JSONObject oidcConfig = new JSONObject();
    oidcConfig.put("issuer", tokenUtils.getIssuerID());
    oidcConfig.put("authorization_endpoint", baseUrl + "/ws/oauth2/authorize");
    oidcConfig.put("token_endpoint", baseUrl + "/ws/oauth2/token");
    oidcConfig.put("userinfo_endpoint", baseUrl + "/ws/oauth2/userinfo");
    oidcConfig.put("scopes_supported", new String[]{ "openid", "email", "profile" });
    oidcConfig.put("id_token_signing_alg_values_supported", new String[]{ tokenUtils.getSignatureAlgorithm() });
    oidcConfig.put("response_types_supported", new String[]{ "code" });
    oidcConfig.put("subject_types_supported", new String[]{ "public" });
    oidcConfig.put("jwks_uri", baseUrl + "/ws/oauth2/certs");
    return oidcConfig;
  }

  private String getBaseUrl( HttpServletRequest request ) {
    if (configurationService.getConfiguration().hasPublicUrl())
      return configurationService.getConfiguration().getPublicUrl();
    if ( ( request.getServerPort() == 80 ) ||
      ( request.getServerPort() == 443 ) )
      return request.getScheme() + "://" +
        request.getServerName() +
        request.getContextPath();
    else
      return request.getScheme() + "://" +
        request.getServerName() + ":" + request.getServerPort() +
        request.getContextPath();
  }

}
