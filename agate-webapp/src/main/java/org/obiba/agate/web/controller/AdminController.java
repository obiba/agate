package org.obiba.agate.web.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.obiba.agate.service.ConfigurationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

<<<<<<< HEAD
import jakarta.inject.Inject;
=======
import javax.inject.Inject;
>>>>>>> a49afd58 (maven resource plugin and admin.ftl)

@Controller
public class AdminController {

  @Inject
  private ConfigurationService configurationService;

  @GetMapping("/admin")
  public ModelAndView admin() {
    Subject subject = SecurityUtils.getSubject();
    String contextPath = configurationService.getContextPath();
    if (!subject.isAuthenticated())
      return new ModelAndView("redirect:signin?redirect=" + contextPath + "/admin");

    if (subject.hasRole("agate-administrator"))
      return new ModelAndView("admin");
    else
      return new ModelAndView("redirect:profile");
  }

<<<<<<< HEAD
}
=======
}
>>>>>>> a49afd58 (maven resource plugin and admin.ftl)
