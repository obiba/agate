package org.obiba.agate.web.controller;

import com.google.common.base.Strings;
import org.obiba.agate.web.support.URLUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class PagesController {

  @GetMapping("/page/{page}")
  public ModelAndView get(HttpServletRequest request, @PathVariable String page) {
    ModelAndView mv = new ModelAndView(page);

    String qs = request.getQueryString();
    if (!Strings.isNullOrEmpty(qs)) {
      Map<String, String> query = URLUtils.queryStringToMap(qs);
      if (!query.isEmpty()) mv.getModel().put("query", query);
    }

    return mv;
  }

}
