package org.obiba.agate.web.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.Subject;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.OidcRealmConfig;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.domain.User;
import org.obiba.agate.security.AgateRealmFactory;
import org.obiba.agate.service.AuthorizationService;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.agate.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;

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

  @GetMapping("/profile")
  public ModelAndView profile() {
    Subject subject = SecurityUtils.getSubject();
    if (!subject.isAuthenticated())
      return new ModelAndView("redirect:signin?redirect=/profile");

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

      return mv;
    } catch (Exception e) {
      return new ModelAndView("redirect:/");
    }
  }

}
