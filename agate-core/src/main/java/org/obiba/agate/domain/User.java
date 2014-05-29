package org.obiba.agate.domain;

import java.util.Set;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.obiba.agate.security.Roles;
import org.obiba.mongodb.domain.AbstractAuditableDocument;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.base.Objects;

/**
 * A user.
 */
@Document
public class User extends AbstractAuditableDocument {

  private static final long serialVersionUID = 688200108221675323L;

  @NotNull
  @Indexed
  private String name;

  private String password;

  private String firstName;

  private String lastName;

  @Email
  private String email;

  private boolean enabled = true;

  private String role = Roles.AGATE_USER.toString();

  private Set<String> groups;

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
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
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
}
