/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.domain;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.joda.time.DateTime;
import org.obiba.agate.security.Roles;
import org.obiba.mongodb.domain.AbstractAuditableDocument;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * A user, for any realm.
 */
@Document
public class User extends AbstractAuditableDocument {

  private static final long serialVersionUID = 688200108221675323L;

  @NotNull
  @Indexed(unique = true)
  private String name;

  private String realm;

  private String firstName;

  private String lastName;

  @Email
  @Indexed(unique = true)
  private String email;

  private Map<String, String> attributes = Maps.newHashMap();

  private UserStatus status = UserStatus.PENDING;

  private String role = Roles.AGATE_USER.toString();

  private Set<String> groups = Sets.newHashSet();

  private Set<String> applications = Sets.newHashSet();

  private DateTime lastLogin;

  private String preferredLanguage;

  public User() {
  }

  public User(String name, String realm) {
    this.name = name;
    this.realm = realm;
  }

  public void setNameAsId() {
    setId(getName().replaceAll("\\s+", "+").toLowerCase());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRealm() {
    return Strings.isNullOrEmpty(realm) ? AgateRealm.AGATE_USER_REALM.getName() : realm;
  }

  public void setRealm(String realm) {
    this.realm = realm;
  }

  public boolean hasFirstName() {
    return !Strings.isNullOrEmpty(firstName);
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public boolean hasLastName() {
    return !Strings.isNullOrEmpty(lastName);
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getDisplayName() {
    String fname = (hasFirstName() ? firstName : "") + " " + (hasLastName() ? lastName : "");
    if (Strings.isNullOrEmpty(fname.trim())) return name;
    return fname.trim();
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public boolean hasAttributes() {
    return attributes.size() > 0;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes == null ? Maps.newHashMap() : attributes;
  }

  public void setAttribute(String name, String value) {
    attributes.put(name, value);
  }

  public void deleteAttribute(String name) {
    attributes.remove(name);
  }

  public boolean isEnabled() {
    return status == UserStatus.ACTIVE;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public void setRole(Roles role) {
    this.role = role.toString();
  }

  /**
   * Check if user is in the specified group.
   *
   * @param group any group if null or empty
   * @return
   */
  public boolean hasGroup(@Nullable String group) {
    return Strings.isNullOrEmpty(group) || groups != null && groups.contains(group);
  }

  /**
   * Check if user is in one of the specified groups.
   *
   * @param groupNames any group if null or empty
   * @return
   */
  public boolean hasOneOfGroup(@Nullable List<String> groupNames) {
    return groupNames == null || groupNames.isEmpty() ||
      hasOneOfGroup(groupNames.toArray(new String[groupNames.size()]));
  }

  /**
   * Check if user is in one of the specified groups.
   *
   * @param groupNames any group if null or empty
   * @return
   */
  public boolean hasOneOfGroup(String... groupNames) {
    if(groupNames == null || groupNames.length == 0) return true;
    if(groups == null || groups.isEmpty()) return false;

    for(String groupName : groupNames) {
      if(groups.contains(groupName)) return true;
    }

    return false;
  }

  public Set<String> getGroups() {
    return groups;
  }

  public boolean hasGroups() {
    return groups != null && !groups.isEmpty();
  }

  public void setGroups(Set<String> groups) {
    this.groups = groups;
  }

  public Set<String> getApplications() {
    return applications;
  }

  public boolean hasApplications() {
    return applications != null && !applications.isEmpty();
  }

  public boolean hasApplication(String application) {
    return hasApplications() && applications.contains(application);
  }

  public void setApplications(Set<String> applications) {
    this.applications = applications;
  }

  public String getPreferredLanguage() {
    return (preferredLanguage == null || preferredLanguage.length() == 0) ? Configuration.DEFAULT_LOCALE.getLanguage() : preferredLanguage;
  }

  public void setPreferredLanguage(String preferredLanguage) {
    this.preferredLanguage = preferredLanguage;
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) {
      return true;
    }
    if(o == null || getClass() != o.getClass()) {
      return false;
    }

    User user = (User) o;

    if(!name.equals(user.name)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  protected Objects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("name", name) //
      .add("firstName", firstName) //
      .add("lastName", lastName) //
      .add("email", email);
  }

  public DateTime getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(DateTime lastLogin) {
    this.lastLogin = lastLogin;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private User user;

    private Builder() {
      user = new User();
      active();
      realm(AgateRealm.AGATE_USER_REALM.getName());
    }

    public Builder id(String id) {
      user.setId(id);
      return this;
    }

    public Builder name(String name) {
      user.setName(name);
      return this;
    }

    public Builder realm(String realm) {
      assert realm != null;
      user.setRealm(realm);
      return this;
    }

    public Builder role(String name) {
      user.setRole(name);
      return this;
    }

    public Builder role(Roles role) {
      user.setRole(role);
      return this;
    }

    public Builder firstName(String name) {
      user.setFirstName(name);
      return this;
    }

    public Builder lastName(String name) {
      user.setLastName(name);
      return this;
    }

    public Builder email(String email) {
      user.setEmail(email);
      return this;
    }

    public Builder status(String status) {
      user.setStatus(UserStatus.valueOf(status));
      return this;
    }

    public Builder preferredLanguage(String preferredLanguage) {
      user.setPreferredLanguage(preferredLanguage);
      return this;
    }

    public Builder active() {
      user.setStatus(UserStatus.ACTIVE);
      return this;
    }

    public Builder inactive() {
      user.setStatus(UserStatus.INACTIVE);
      return this;
    }

    public Builder pending() {
      user.setStatus(UserStatus.PENDING);
      return this;
    }

    public Builder with(String name, String value) {
      user.setAttribute(name, value);
      return this;
    }

    public Builder groups(String... groups) {
      user.setGroups(Sets.newHashSet(groups));
      return this;
    }

    public Builder groups(List<String> groups) {
      user.setGroups(Sets.newHashSet(groups));
      return this;
    }

    public Builder applications(String... applications) {
      user.setApplications(Sets.newHashSet(applications));
      return this;
    }

    public Builder applications(List<String> applications) {
      user.setApplications(Sets.newHashSet(applications));
      return this;
    }

    public Builder attribute(String name, String value) {
      user.setAttribute(name, value);
      return this;
    }

    public Builder attributes(Map<String, String> attributes) {
      user.setAttributes(attributes);
      return this;
    }

    public User build() {
      return user;
    }
  }
}
