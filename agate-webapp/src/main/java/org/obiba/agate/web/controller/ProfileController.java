package org.obiba.agate.web.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.obiba.agate.config.ClientConfiguration;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.OidcRealmConfig;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.AuthorizationService;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.controller.domain.AuthConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

import static org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE_REQUEST_ATTRIBUTE_NAME;

@Controller
public class ProfileController {

  @Inject
  private UserService userService;

  @Inject
  private RealmConfigService realmConfigService;

  @Inject
  private ConfigurationService configurationService;

  @Inject
  protected AuthorizationService authorizationService;

  @Inject
  private ClientConfiguration clientConfiguration;

  @GetMapping("/profile")
  public ModelAndView profile(HttpServletRequest request) {
    Subject subject = SecurityUtils.getSubject();
    if (!subject.isAuthenticated())
      return new ModelAndView("redirect:signin?redirect=" + configurationService.getContextPath() + "/profile");

    try {
      User user = userService.getCurrentUser();
      ModelAndView mv = new ModelAndView("profile");
      mv.getModel().put("applications", userService.getUserApplications(user));

      RealmConfig realmConfig = realmConfigService.findConfig(user.getRealm());
      if (realmConfig != null && realmConfig.getType().equals(AgateRealm.AGATE_OIDC_REALM)) {
        mv.getModel().put("realmConfig", realmConfig);
        OidcRealmConfig oidcRealmConfig = OidcRealmConfig.newBuilder(configurationService.decrypt(realmConfig.getContent())).build();
        mv.getModel().put("providerUrl", oidcRealmConfig.getCustomParam("providerUrl"));
      }

      mv.getModel().put("authorizations", authorizationService.list(user.getName()));
      mv.getModel().put("authConfig", new AuthConfiguration(configurationService.getConfiguration(), clientConfiguration));

      try {
        Locale locale = Locale.forLanguageTag(user.getPreferredLanguage());
        if (locale != null)
          request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME, locale);
      } catch (Exception e) {
      }

      return mv;
    } catch (Exception e) {
      return new ModelAndView("redirect:" + configurationService.getContextPath() + "/");
    }
  }

}
