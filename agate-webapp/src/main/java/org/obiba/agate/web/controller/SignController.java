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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.obiba.agate.config.ClientConfiguration;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserStatus;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.controller.domain.AuthConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;

@Controller
public class SignController {

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private ClientConfiguration clientConfiguration;

  @Inject
  private UserService userService;

  @GetMapping("/signin")
  public ModelAndView signin() {
    Subject subject = SecurityUtils.getSubject();
    if (subject.isAuthenticated())
      return new ModelAndView("redirect:/");

    ModelAndView mv = new ModelAndView("signin");
    return mv;
  }

  @GetMapping("/signup")
  public ModelAndView signup() {
    if (!configurationService.getConfiguration().isJoinPageEnabled())
      return new ModelAndView("redirect:/");

    Subject subject = SecurityUtils.getSubject();
    if (subject.isAuthenticated())
      return new ModelAndView("redirect:/");

    ModelAndView mv = new ModelAndView("signup");

    mv.getModel().put("authConfig", new AuthConfiguration(configurationService.getConfiguration(), clientConfiguration));

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


}
