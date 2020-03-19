package org.obiba.agate.web.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;

@Controller
public class ProfileController {

  @Inject
  private UserService userService;

  @GetMapping("/profile")
  public ModelAndView profile() {
    Subject subject = SecurityUtils.getSubject();
    if (!subject.isAuthenticated())
      return new ModelAndView("redirect:signin?redirect=profile");

    try {
      User user = userService.getCurrentUser();
      ModelAndView mv = new ModelAndView("profile");
      mv.getModel().put("applications", userService.getUserApplications(user));
      return mv;
    } catch (Exception e) {
      return new ModelAndView("redirect:/");
    }
  }

}
