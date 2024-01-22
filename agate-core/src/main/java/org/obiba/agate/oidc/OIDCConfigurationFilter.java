package org.obiba.agate.oidc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Implements the well-known entry point that provides the settings of OpenID Connect.
 */
public class OIDCConfigurationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(OIDCConfigurationFilter.class);

  private TokenUtils tokenUtils;

  private final ConfigurationService configurationService;

  public OIDCConfigurationFilter(TokenUtils tokenUtils, ConfigurationService configurationService) {
    this.tokenUtils = tokenUtils;
    this.configurationService = configurationService;
  }


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
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
    String baseURL = configurationService.getBaseURL(request);
    JSONObject oidcConfig = new JSONObject();
    oidcConfig.put("issuer", tokenUtils.getIssuerID());
    oidcConfig.put("authorization_endpoint", baseURL + "/ws/oauth2/authorize");
    oidcConfig.put("token_endpoint", baseURL + "/ws/oauth2/token");
    oidcConfig.put("end_session_endpoint", baseURL + "/ws/oauth2/logout");
    oidcConfig.put("userinfo_endpoint", baseURL + "/ws/oauth2/userinfo");
    oidcConfig.put("scopes_supported", new String[]{"openid", "email", "profile"});
    oidcConfig.put("id_token_signing_alg_values_supported", new String[]{tokenUtils.getSignatureAlgorithm()});
    oidcConfig.put("response_types_supported", new String[]{"code"});
    oidcConfig.put("subject_types_supported", new String[]{"public"});
    oidcConfig.put("jwks_uri", baseURL + "/ws/oauth2/certs");
    return oidcConfig;
  }

}
