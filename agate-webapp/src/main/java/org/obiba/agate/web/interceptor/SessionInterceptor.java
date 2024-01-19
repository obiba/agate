package org.obiba.agate.web.interceptor;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.obiba.agate.domain.User;
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

@Component
public class SessionInterceptor implements HandlerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(SessionInterceptor.class);

  private final UserService userService;

  @Inject
  public SessionInterceptor(UserService userService) {
    this.userService = userService;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    if (modelAndView == null) return;
    Subject subject = SecurityUtils.getSubject();
    if (subject.isAuthenticated()) {
      String username = subject.getPrincipal().toString();
      modelAndView.getModel().put("username", username);
      try {
        User user = userService.getCurrentUser();
        modelAndView.getModel().put("user", new UserProfile(user));
      } catch (Exception e) {
        // user from Ini realm
        log.debug("User {} is not a Agate regular user", username);
      }
    }
  }

}
