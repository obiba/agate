package org.obiba.agate.web.controller.domain;

import org.obiba.agate.domain.User;
import org.owasp.esapi.ESAPI;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Safe user profile.
 */
public class UserProfile {

  private final User user;

  public UserProfile(User user) {
    this.user = user;
  }

  public String getName() {
    return user.getName();
  }

  public String getEmail() {
    return user.getEmail();
  }

  public String getRole() {
    return user.getRole();
  }

  public Set<String> getGroups() {
    return user.getGroups();
  }

  public Set<String> getApplications() {
    return user.getApplications();
  }

  public String getFirstName() {
    return ESAPI.encoder().encodeForHTML(user.getFirstName());
  }

  public String getLastName() {
    return ESAPI.encoder().encodeForHTML(user.getLastName());
  }

  public String getDisplayName() {
    return ESAPI.encoder().encodeForHTML(user.getDisplayName());
  }

  public String getPreferredLanguage() {
    return user.getPreferredLanguage();
  }

  public String getRealm() {
    return user.getRealm();
  }



  public boolean hasAttributes() {
    return user.hasAttributes();
  }

  public Map<String, String> getAttributes() {
    return user.getAttributes().entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> ESAPI.encoder().encodeForHTML(e.getValue())));
  }

}
