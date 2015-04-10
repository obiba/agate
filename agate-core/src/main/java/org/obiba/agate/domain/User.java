package org.obiba.agate.domain;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.obiba.agate.security.AgateUserRealm;
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
  private String email;

  private Map<String,String> attributes = Maps.newHashMap();

  private UserStatus status = UserStatus.PENDING;

  private String role = Roles.AGATE_USER.toString();

  private Set<String> groups;

  private Set<String> applications;

  public User() {
  }

  public User(String name, String realm) {
    this.name = name;
    this.realm = realm;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRealm() {
    return realm;
  }

  public void setRealm(String realm) {
    this.realm = realm;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
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

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    User user;

    private Builder() {
      user = new User();
      active();
      realm(AgateUserRealm.AGATE_REALM);
    }

    public Builder name(String name) {
      user.setName(name);
      return this;
    }

    public Builder realm(String realm) {
      user.setRealm(Strings.isNullOrEmpty(realm) ? AgateUserRealm.AGATE_REALM : realm);
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

    public User build() {
      return user;
    }
  }
}
