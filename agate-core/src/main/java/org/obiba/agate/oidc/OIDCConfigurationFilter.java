package org.obiba.agate.oidc;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
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
import java.util.Map;

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
    // Be flexible with entry point as agate could be accessed from different host name
    Map<String, String> queryMap = parseQuery(request.getQueryString());
    String host = request.getHeader("Host");
    if (Strings.isNullOrEmpty(host) && queryMap.containsKey("host"))
      host = queryMap.get("host");
    String baseURL;
    if (Strings.isNullOrEmpty(host))
      baseURL = configurationService.getPublicUrl();
    else if (Strings.isNullOrEmpty(configurationService.getContextPath()))
      baseURL = host;
    else
      baseURL = String.format("%s%s", host, configurationService.getContextPath());
    JSONObject oidcConfig = new JSONObject();
    oidcConfig.put("issuer", tokenUtils.getIssuerID());
    oidcConfig.put("authorization_endpoint", baseURL + "/ws/oauth2/authorize");
    oidcConfig.put("token_endpoint", baseURL + "/ws/oauth2/token");
    oidcConfig.put("userinfo_endpoint", baseURL + "/ws/oauth2/userinfo");
    oidcConfig.put("scopes_supported", new String[]{"openid", "email", "profile"});
    oidcConfig.put("id_token_signing_alg_values_supported", new String[]{tokenUtils.getSignatureAlgorithm()});
    oidcConfig.put("response_types_supported", new String[]{"code"});
    oidcConfig.put("subject_types_supported", new String[]{"public"});
    oidcConfig.put("jwks_uri", baseURL + "/ws/oauth2/certs");
    return oidcConfig;
  }

  private Map<String, String> parseQuery(String query) {
    Map<String, String> queryMap = Maps.newHashMap();
    if (Strings.isNullOrEmpty(query)) return queryMap;

    for (String pair : query.split("&")) {
      int idxOfEqual = pair.indexOf("=");

      if (idxOfEqual > 0) {
        String key = pair.substring(0, idxOfEqual);
        String value = pair.substring(idxOfEqual + 1);
        queryMap.put(key, value);
      }
    }
    return queryMap;
  }

}
