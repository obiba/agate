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

import javax.validation.constraints.NotNull;

import org.obiba.mongodb.domain.AbstractAuditableDocument;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.base.Objects;

/**
 * User credentials for the {@link org.obiba.agate.security.AgateUserRealm}.
 */
@Document
public class UserCredentials extends AbstractAuditableDocument {

  private static final long serialVersionUID = 688200108221675323L;

  @NotNull
  @Indexed(unique = true)
  private String name;

  private String password;

  public UserCredentials() {
  }

  public UserCredentials(String name, String password) {
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

  @Override
  public boolean equals(Object o) {
    if(this == o) {
      return true;
    }
    if(o == null || getClass() != o.getClass()) {
      return false;
    }

    UserCredentials user = (UserCredentials) o;

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
  public String toString() {
    return "UserCredentials{" +
        "name='" + name + '\'' +
        '}';
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    UserCredentials user;

    private Builder() {
      user = new UserCredentials();
    }

    public Builder name(String name) {
      user.setName(name);
      return this;
    }

    public Builder password(String pwd) {
      user.setPassword(pwd);
      return this;
    }

    public UserCredentials build() {
      return user;
    }
  }
}
