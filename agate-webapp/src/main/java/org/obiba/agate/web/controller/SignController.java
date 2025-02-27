/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.controller;

import com.google.common.base.Strings;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.config.ClientConfiguration;
import org.obiba.agate.domain.Application;
import org.obiba.agate.domain.RealmUsage;
import org.obiba.agate.domain.User;
import org.obiba.agate.security.OidcAuthConfigurationProvider;
import org.obiba.agate.service.ApplicationService;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.NoSuchUserException;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.controller.domain.AuthConfiguration;
import org.obiba.agate.web.controller.domain.OidcProvider;
import org.obiba.agate.web.support.URLUtils;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.oidc.utils.OIDCHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import jakarta.inject.Inject;
import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class SignController {

  private static final Logger log = LoggerFactory.getLogger(SignController.class);

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private ClientConfiguration clientConfiguration;

  @Inject
  private OidcAuthConfigurationProvider oidcAuthConfigurationProvider;

  @Inject
  private ApplicationService applicationService;

  @Inject
  private UserService userService;

  @Value("${logout.confirm:false}")
  private boolean confirmLogout;

  /**
   * Controller for redirect URL.
   *
   * @param redirect
   * @return
   */
  @GetMapping("/check")
  public ModelAndView check(@RequestParam(value = "redirect", required = false) String redirect) {
    String verifiedRedirect = normalizeRedirect(verifyRedirect(redirect));
    return new ModelAndView("redirect:" + (Strings.isNullOrEmpty(verifiedRedirect) ? configurationService.getContextPath() + "/" : verifiedRedirect));
  }

  @GetMapping("/signin")
  public ModelAndView signin(HttpServletRequest request,
                             @CookieValue(value = "NG_TRANSLATE_LANG_KEY", required = false, defaultValue = "en") String locale,
                             @RequestParam(value = "language", required = false) String language,
                             @RequestParam(value = "redirect", required = false) String redirect) {
    Subject subject = SecurityUtils.getSubject();
    String verifiedRedirect = normalizeRedirect(verifyRedirect(redirect));
    if (subject.isAuthenticated())
      return new ModelAndView("redirect:" + (Strings.isNullOrEmpty(verifiedRedirect) ? configurationService.getContextPath() + "/" : verifiedRedirect));

    ModelAndView mv = new ModelAndView("signin");

    mv.getModel().put("oidcProviders", getOidcProviders(RealmUsage.ALL, getLang(language, locale),
        "redirect=" + (Strings.isNullOrEmpty(verifiedRedirect) ? configurationService.getContextPath() + "/" : URLUtils.encode(verifiedRedirect)) + "&signin_error=" + configurationService.getContextPath() + "/signup-with",
        configurationService.getContextPath()));
    return mv;
  }

  @GetMapping("/signup")
  public ModelAndView signup(@CookieValue(value = "NG_TRANSLATE_LANG_KEY", required = false, defaultValue = "en") String locale,
                             @RequestParam(value = "language", required = false) String language) {
    if (!configurationService.getConfiguration().isJoinPageEnabled())
      return new ModelAndView("redirect:" + configurationService.getContextPath() + "/");

    Subject subject = SecurityUtils.getSubject();
    if (subject.isAuthenticated())
      return new ModelAndView("redirect:" + configurationService.getContextPath() + "/");

    ModelAndView mv = new ModelAndView("signup");
    mv.getModel().put("authConfig", new AuthConfiguration(configurationService.getConfiguration(), clientConfiguration));
    mv.getModel().put("oidcProviders", getOidcProviders(RealmUsage.SIGNUP, getLang(language, locale),
        "signin_error=" + configurationService.getContextPath() + "/signup-with",
        configurationService.getContextPath()));
    return mv;
  }

  @GetMapping("/signup-with")
  public ModelAndView signupWith(@CookieValue(value = "u_auth", required = false, defaultValue = "{}") String uAuth, @RequestParam(value = "redirect", required = false) String redirect) {
    if (!configurationService.getConfiguration().isJoinPageEnabled())
      return new ModelAndView("redirect:" + configurationService.getContextPath() + "/");

    ModelAndView mv = new ModelAndView("signup-with");
    mv.getModel().put("authConfig", new AuthConfiguration(configurationService.getConfiguration(), clientConfiguration));
    JSONObject uAuthObj;
    try {
      String fixedUAuth = uAuth.replaceAll("\\\\", "");
      uAuthObj = new JSONObject(fixedUAuth);
    } catch (JSONException e) {
      uAuthObj = new JSONObject();
    }

    log.debug("Signup with username {}", uAuth);
    mv.getModel().put("uAuth", uAuthObj.toMap());
    if (uAuthObj.has("username") || Strings.isNullOrEmpty(redirect)) {
      return mv;
    }
    return check(redirect);
  }

  @GetMapping("/signout")
  public ModelAndView signout(@RequestParam(value = "post_logout_redirect_uri", required = false) String postLogoutRedirectUri) {
    Subject subject = SecurityUtils.getSubject();
    if (!subject.isAuthenticated()) {
      ModelAndView mv = new ModelAndView("signout");
      mv.getModel().put("authenticated", false);
      mv.getModel().put("confirm", false);
      mv.getModel().put("postLogoutRedirectUri", ensurePostLogoutRedirectUri(postLogoutRedirectUri));
      return mv;
    }

    String newPostLogoutRedirectUri = postLogoutRedirectUri;
    try {
      User user = userService.getCurrentUser();

      // Validate redirect uri against user's associated applications
      if (!Strings.isNullOrEmpty(postLogoutRedirectUri)) {
        Set<String> appNames = userService.getUserApplications(user);
        boolean redirectIsValid = false;
        for (String appName : appNames) {
          Application application = applicationService.findByIdOrName(appName);
          if (application != null && application.getRedirectURIs().stream().anyMatch(postLogoutRedirectUri::startsWith)) {
            redirectIsValid = true;
            break;
          }
        }
        if (!redirectIsValid) {
          ModelAndView mv = new ModelAndView("redirect:error");
          mv.getModel().put("error", "400");
          mv.getModel().put("message", "invalid-redirect");
          return mv;
        }
      }

      OIDCConfiguration oidcConfig = oidcAuthConfigurationProvider.getConfiguration(user.getRealm());
      if (oidcConfig != null) {
        try {
          OIDCProviderMetadata metadata = OIDCHelper.discoverProviderMetaData(oidcConfig);
          URI logoutEndpoint = metadata.getEndSessionEndpointURI();
          if (logoutEndpoint != null) {
            // tell OIDC provider to logout and redirect to the agate's signout page with its own redirection...
            log.debug("Using {} OIDC logout endpoint: {}", user.getRealm(), logoutEndpoint);
            String logoutRedirect = String.format("%s/signout", configurationService.getPublicUrl());
            if (!Strings.isNullOrEmpty(postLogoutRedirectUri)) {
              logoutRedirect = UriBuilder.fromUri(logoutRedirect)
                  .queryParam("post_logout_redirect_uri", postLogoutRedirectUri)
                  .build().toString();
            }
            UriBuilder oidcLogoutURIBuilder = UriBuilder.fromUri(logoutEndpoint);
            if (!Strings.isNullOrEmpty(logoutRedirect)) {
              oidcLogoutURIBuilder.queryParam("post_logout_redirect_uri", logoutRedirect);
            }
            oidcLogoutURIBuilder.queryParam("client_id", oidcConfig.getClientId());
            newPostLogoutRedirectUri = oidcLogoutURIBuilder.build().toString();
          }
        } catch (Exception e) {
          log.error("Error when getting OIDC logout URL {}", user.getRealm(), e);
        }
      }
    } catch (NoSuchUserException e) {
      // ignore
    }

    ModelAndView mv = new ModelAndView("signout");
    mv.getModel().put("authenticated", true);
    mv.getModel().put("confirm", confirmLogout);
    mv.getModel().put("postLogoutRedirectUri", ensurePostLogoutRedirectUri(newPostLogoutRedirectUri));
    return mv;
  }

  @GetMapping("/just-registered")
  public ModelAndView justRegistered(@RequestParam(value = "signin", required = false, defaultValue = "false") boolean canSignin) {
    ModelAndView mv = new ModelAndView("just-registered");
    mv.getModel().put("canSignin", canSignin);
    return mv;
  }

  @GetMapping("/confirm")
  public ModelAndView reset(@RequestParam(value = "key", required = false) String key) {
    ModelAndView mv = new ModelAndView("confirm");
    mv.getModel().put("key", key);
    return mv;
  }

  //
  // Private methods
  //

  private String getLang(String language, String locale) {
    return language == null ? locale : language;
  }

  private Collection<OidcProvider> getOidcProviders(RealmUsage usage, String locale, String query, String contextPath) {
    return oidcAuthConfigurationProvider.getConfigurations(usage).stream()
        .map(conf -> new OidcProvider(conf, locale, query, contextPath))
        .collect(Collectors.toList());
  }

  private String ensurePostLogoutRedirectUri(String postLogoutRedirectUri) {
    if (!Strings.isNullOrEmpty(postLogoutRedirectUri)) {
      return normalizeRedirect(verifyRedirect(postLogoutRedirectUri));
    }
    String url = configurationService.getConfiguration().getPortalUrl();
    if (!Strings.isNullOrEmpty(url)) return url;

    url = configurationService.getConfiguration().hasPublicUrl() ? configurationService.getPublicUrl() : "/";
    String contextPath = configurationService.getContextPath();
    return contextPath.equals("/") ? url : url + contextPath;
  }

  private String normalizeRedirect(String redirect) {
    if (Strings.isNullOrEmpty(redirect)) return redirect;
    String contextPath = configurationService.getContextPath();
    return !redirect.equals("/") && redirect.startsWith(contextPath) ? redirect.replace(contextPath, "") : redirect;
  }

  private String verifyRedirect(String redirect) {
    if (Strings.isNullOrEmpty(redirect) || redirect.startsWith("/")) return redirect;
    if (!redirect.startsWith("http")) return "";
    // redirect to itself
    String publicUrl = configurationService.getPublicUrl();
    if (redirect.startsWith(publicUrl)) return redirect;
    // redirect to a registered application
    boolean isAppRedirect = applicationService.findAll().stream().anyMatch((app) -> app.hasRedirectURI() && app.getRedirectURIs().stream().anyMatch(redirect::startsWith));
    if (isAppRedirect) return redirect;
    // redirect to a OIDC service, assumption is that host name is same as the one from the discovery url
    try {
      URI redirectURI = URI.create(redirect);
      boolean hasMatch = oidcAuthConfigurationProvider.getConfigurations().stream().anyMatch(conf -> {
        try {
          URI discoveryURI = URI.create(conf.getDiscoveryURI());
          return discoveryURI.getHost().equals(redirectURI.getHost());
        } catch (Exception e) {
          return false;
        }
      });
      return hasMatch ? redirect : "";
    } catch (Exception e) {
      return "";
    }
  }

}
