package org.obiba.agate.domain;


import java.util.Map;
import java.util.Set;

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
    return user.getFirstName();
  }

  public String getLastName() {
    return user.getLastName();
  }

  public String getDisplayName() {
    return user.getDisplayName();
  }

  public String getPreferredLanguage() {
    return user.getPreferredLanguage();
  }

  public boolean getOtpEnabled() {
    return user.hasSecret();
  }

  public String getRealm() {
    return user.getRealm();
  }

  public boolean hasAttributes() {
    return user.hasAttributes();
  }

  public Map<String, String> getAttributes() {
    return user.getAttributes();
  }

}
