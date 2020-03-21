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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ResetForgotPasswordController {

  @GetMapping("/reset-password")
  public ModelAndView reset(@RequestParam(value = "key", required = false) String key) {
    ModelAndView mv = new ModelAndView("reset-password");
    mv.getModel().put("key", key);
    return mv;
  }

  @GetMapping("/forgot-password")
  public ModelAndView forgot() {
    ModelAndView mv = new ModelAndView("forgot-password");
    return mv;
  }
}
