package org.obiba.agate.web.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.obiba.agate.service.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.inject.Inject;
import java.io.IOException;

@Controller
public class AdminController {

  private static final Logger log = LoggerFactory.getLogger(AdminController.class);

  @Inject
  private ConfigurationService configurationService;

  @GetMapping("/admin")
  public ModelAndView admin() {
    Subject subject = SecurityUtils.getSubject();
    String contextPath = configurationService.getContextPath();
    if (!subject.isAuthenticated())
      return new ModelAndView("redirect:signin?redirect=" + contextPath + "/admin");

    if (subject.hasRole("agate-administrator")) {
      ModelAndView mv = new ModelAndView("admin");
      try {
        includeEntryPoints(mv);
      } catch (IOException e) {
        log.error("Error while reading SPA entry points", e);
      }
      return mv;
    } else
      return new ModelAndView("redirect:profile");
  }

  private void includeEntryPoints(ModelAndView mv) throws IOException {
    var resolver = new PathMatchingResourcePatternResolver();
    String folderPath = "classpath:/static/admin/assets/*";

    // Resolve all resources under the folder
    Resource[] resources = resolver.getResources(folderPath);

    for (Resource resource : resources) {
      String fileName = resource.getFilename();
      if (fileName != null && fileName.startsWith("index.")) {
        log.info("Quasar entrypoint: {}", fileName);
        if (fileName.endsWith(".js")) {
          mv.getModel().put("entryPointJS", fileName);
        } else if (fileName.endsWith(".css")) {
          mv.getModel().put("entryPointCSS", fileName);
        }
      }
    }
  }

}
