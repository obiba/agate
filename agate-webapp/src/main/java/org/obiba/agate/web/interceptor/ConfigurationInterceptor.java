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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ConfigurationInterceptor extends HandlerInterceptorAdapter {

  private final ConfigurationService configurationService;

  @Inject
  public ConfigurationInterceptor(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    modelAndView.getModel().put("config", configurationService.getConfiguration());
    modelAndView.getModel().put("contextPath", configurationService.getContextPath());
  }
}
