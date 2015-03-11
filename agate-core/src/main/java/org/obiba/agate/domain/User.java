package org.obiba.agate.domain;

import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.obiba.agate.security.Roles;
import org.obiba.mongodb.domain.AbstractAuditableDocument;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

/**
 * A user.
 */
@Document
public class User extends AbstractAuditableDocument {

  private static final long serialVersionUID = 688200108221675323L;

  @NotNull
  @Indexed(unique = true)
  private String name;

  private String password;

  private String firstName;

  private String lastName;

  @Email
  private String email;

  private UserStatus status = UserStatus.PENDING;

  private String role = Roles.AGATE_USER.toString();

  private Set<String> groups;

  public User() {
  }

  public User(String name, String password) {
    this.name = name;
    this.password = password;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
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
    }

    public Builder name(String name) {
      user.setName(name);
      return this;
    }

    public Builder password(String pwd) {
      user.setPassword(pwd);
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

    public Builder groups(String... groups) {
      user.setGroups(Sets.newHashSet(groups));
      return this;
    }

    public Builder groups(List<String> groups) {
      user.setGroups(Sets.newHashSet(groups));
      return this;
    }

    public User build() {
      return user;
    }
  }
}
