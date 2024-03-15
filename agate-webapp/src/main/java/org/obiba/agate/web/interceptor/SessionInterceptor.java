package org.obiba.agate.web.interceptor;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.RealmConfigService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.controller.domain.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.stream.Collectors;

@Component
public class SessionInterceptor implements HandlerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(SessionInterceptor.class);

  private final UserService userService;

  private final RealmConfigService realmConfigService;

  @Inject
  public SessionInterceptor(UserService userService, RealmConfigService realmConfigService) {
    this.userService = userService;
    this.realmConfigService = realmConfigService;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    if (modelAndView == null) return;
    Subject subject = SecurityUtils.getSubject();
    boolean otpSupport = true;
    if (subject.isAuthenticated()) {
      String username = subject.getPrincipal().toString();
      modelAndView.getModel().put("username", username);
      try {
        User user = userService.getCurrentUser();
        RealmConfig realmConfig = realmConfigService.findConfig(user.getRealm());
        if (realmConfig != null) {
          modelAndView.getModel().put("realmType", realmConfig.getType().getName());
          otpSupport = !AgateRealm.AGATE_OIDC_REALM.equals(realmConfig.getType());
        } else {
          modelAndView.getModel().put("realmType", String.join(",", subject.getPrincipals().getRealmNames()));
        }
        modelAndView.getModel().put("user", new UserProfile(user));
        modelAndView.getModel().put("otpEnabled", user.hasSecret());
      } catch (Exception e) {
        // user from Ini realm
        log.debug("User {} is not a Agate regular user", username);
        modelAndView.getModel().put("realmType", String.join(",", subject.getPrincipals().getRealmNames()));
      }
      modelAndView.getModel().put("otpSupport", otpSupport);
      modelAndView.getModel().put("realm", subject.getPrincipals().getRealmNames().stream().findFirst().orElse(null));
    }
  }

}
