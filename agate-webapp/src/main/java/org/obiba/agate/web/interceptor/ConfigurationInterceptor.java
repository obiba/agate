/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.interceptor;

import org.obiba.agate.service.ConfigurationService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ConfigurationInterceptor implements HandlerInterceptor {

  private final ConfigurationService configurationService;

  @Inject
  public ConfigurationInterceptor(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    if (modelAndView == null) return;
    modelAndView.getModel().put("config", configurationService.getConfiguration());
  }
}
