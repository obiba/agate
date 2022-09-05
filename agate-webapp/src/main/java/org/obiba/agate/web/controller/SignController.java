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
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.json.JSONException;
import org.json.JSONObject;
import org.obiba.agate.config.ClientConfiguration;
import org.obiba.agate.domain.*;
import org.obiba.agate.security.OidcAuthConfigurationProvider;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.controller.domain.AuthConfiguration;
import org.obiba.agate.web.controller.domain.OidcProvider;
import org.obiba.agate.web.support.URLUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE_REQUEST_ATTRIBUTE_NAME;

@Controller
public class SignController {

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private ClientConfiguration clientConfiguration;

  @Inject
  private OidcAuthConfigurationProvider oidcAuthConfigurationProvider;

  @Inject
  private UserService userService;

  @GetMapping("/signin")
  public ModelAndView signin(HttpServletRequest request,
                             @CookieValue(value = "NG_TRANSLATE_LANG_KEY", required = false, defaultValue = "en") String locale,
                             @RequestParam(value = "language", required = false) String language,
                             @RequestParam(value = "redirect", required = false) String redirect) {
    Subject subject = SecurityUtils.getSubject();
    if (subject.isAuthenticated())
      return new ModelAndView("redirect:" + (Strings.isNullOrEmpty(redirect) ? configurationService.getContextPath() + "/" : redirect));

    ModelAndView mv = new ModelAndView("signin");

    mv.getModel().put("oidcProviders", getOidcProviders(RealmUsage.ALL, getLang(language, locale),
      "redirect=" + (Strings.isNullOrEmpty(redirect) ? configurationService.getContextPath() + "/" : URLUtils.encode(redirect)) + "&signin_error=" + configurationService.getContextPath() + "/signup-with",
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
  public ModelAndView signupWith(@CookieValue(value = "u_auth", required = false, defaultValue = "{}") String uAuth) {
    if (!configurationService.getConfiguration().isJoinPageEnabled())
      return new ModelAndView("redirect:" + configurationService.getContextPath() + "/");

    ModelAndView mv = new ModelAndView("signup-with");
    JSONObject uAuthObj;
    try {
      String fixedUAuth = uAuth.replaceAll("\\\\", "");
      uAuthObj = new JSONObject(fixedUAuth);
    } catch (JSONException e) {
      uAuthObj = new JSONObject();
    }

    if (uAuthObj.has("username")) {
      mv.getModel().put("uAuth", uAuthObj);
      mv.getModel().put("authConfig", new AuthConfiguration(configurationService.getConfiguration(), clientConfiguration));
      return mv;
    }
    return new ModelAndView("redirect:/signup");
  }

  @GetMapping("/signout")
  public ModelAndView signout(@RequestParam(value = "post_logout_redirect_uri", required = false) String postLogoutRedirectUri) {
    Subject subject = SecurityUtils.getSubject();
    if (!subject.isAuthenticated()) {
      if (!Strings.isNullOrEmpty(postLogoutRedirectUri)) {
        return new ModelAndView("redirect:" + postLogoutRedirectUri);
      }
      // for consistency
      return new ModelAndView("redirect:signin?redirect=" + configurationService.getContextPath() + "/signout");
    }

    try {
      ModelAndView mv = new ModelAndView("signout");
      mv.getModel().put("postLogoutRedirectUri", postLogoutRedirectUri);
      return mv;
    } catch (Exception e) {
      return new ModelAndView("redirect:" + configurationService.getContextPath() + "/");
    }
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

  private String getLang(String language, String locale) {
    return language == null ? locale : language;
  }

  private Collection<OidcProvider> getOidcProviders(RealmUsage usage, String locale, String query, String contextPath) {
    return oidcAuthConfigurationProvider.getConfigurations(usage).stream()
      .map(conf -> new OidcProvider(conf, locale, query, contextPath))
      .collect(Collectors.toList());
  }

}
