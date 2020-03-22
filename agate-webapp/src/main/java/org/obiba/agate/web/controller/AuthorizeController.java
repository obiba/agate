package org.obiba.agate.web.controller;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.obiba.agate.domain.Application;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.ApplicationService;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.support.URLUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class AuthorizeController {

  private final List<String> AGATE_SCOPES = Lists.newArrayList("openid", "email", "profile");

  @Inject
  private UserService userService;

  @Inject
  private ApplicationService applicationService;

  @Inject
  private ConfigurationService configurationService;

  @GetMapping("/authorize")
  public ModelAndView authz(HttpServletRequest request,
                            @RequestParam(value = "response_type") String responseType,
                            @RequestParam(value = "client_id") String clientId,
                            @RequestParam(value = "redirect_uri") String redirectUri,
                            @RequestParam(value = "scope") String scope,
                            @RequestParam(value = "state") String state) {
    Subject subject = SecurityUtils.getSubject();
    String qs = request.getQueryString();

    if (!subject.isAuthenticated()) {
      String redirect = configurationService.getPublicUrl() + "/ws/oauth2/authorize?" + qs;
      return new ModelAndView("redirect:signin?redirect=" + URLUtils.encode(redirect));
    }

    try {
      User user = userService.getCurrentUser();
      ModelAndView mv = new ModelAndView("authorize");

      mv.getModel().put("responseType", responseType);
      mv.getModel().put("clientId", clientId);
      mv.getModel().put("redirectUri", redirectUri);
      mv.getModel().put("scope", scope);
      mv.getModel().put("state", state);

      Application application = applicationService.find(clientId);
      mv.getModel().put("application", application);

      Set<String> applicationNames = userService.getUserApplications(user);
      mv.getModel().put("applicationAuthorized", application != null && applicationNames.contains(clientId));

      if (application != null) {
        Map<String, ApplicationBundle> applicationScopes = Maps.newLinkedHashMap();

        Set<String> scopeNames = Sets.newLinkedHashSet(Splitter.on(" ").split(scope));
        Set<Application> applications = applicationNames.stream().map(n -> applicationService.getApplication(n))
          .collect(Collectors.toSet());


        for (String scopeName : scopeNames) {
          if (AGATE_SCOPES.contains(scopeName)) {
            if (!applicationScopes.containsKey("_agate")) {
              ApplicationBundle bundle = new ApplicationBundle("_agate", "oauth.openid-request");
              applicationScopes.put(bundle.getName(), bundle);
            }
            applicationScopes.get("_agate").addScope(new Application.Scope(scopeName, "oauth.openid-" + scopeName + "-description"));
          } else {
            String[] scopeParts = scopeName.split(":");
            Application app = findApplication(applications, scopeParts[0]);
            if (app != null) {
              if (!applicationScopes.containsKey(app.getId())) {
                applicationScopes.put(app.getId(), new ApplicationBundle(app));
              }
              if (scopeParts.length > 1 && app.hasScope(scopeParts[1])) {
                applicationScopes.get(app.getId()).addScope(app.getScope(scopeParts[1]));
              }
            }
          }
        }

        mv.getModel().put("scope", Joiner.on(" ").join(scopeNames)); // ensure only valid scopes are handled
        mv.getModel().put("applicationScopes", applicationScopes);
      }

      return mv;
    } catch (Exception e) {
      return new ModelAndView("redirect:/");
    }
  }

  private Application findApplication(Set<Application> applications, String id) {
    return applications.stream().filter(app -> app.getId().equals(id)).findFirst().orElse(null);
  }

  public class ApplicationBundle {
    private final String id;
    private final String name;
    private final String description;
    private final List<Application.Scope> scopes = Lists.newArrayList();

    public ApplicationBundle(String name, String description) {
      this.id = name;
      this.name = name;
      this.description = description;
    }

    public ApplicationBundle(Application application) {
      this.id = application.getId();
      this.name = application.getName();
      this.description = application.getDescription();
    }

    public String getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }

    public void addScope(Application.Scope scope) {
      getScopes().add(scope);
    }

    public List<Application.Scope> getScopes() {
      return scopes;
    }
  }

}
