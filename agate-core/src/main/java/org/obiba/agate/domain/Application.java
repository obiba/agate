/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.domain;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.mongodb.domain.AbstractAuditableDocument;
import org.springframework.data.mongodb.core.index.Indexed;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class Application extends AbstractAuditableDocument {

  private static final long serialVersionUID = 4710884170897922907L;

  @NotNull
  @Indexed(unique = true)
  private String name;

  private String description;

  private String key;

  private String redirectURI;

  private List<Scope> scopes;

  public Application() {
  }

  public Application(String name) {
    this.name = name;
  }

  public Application(String name, String key) {
    this.name = name;
    this.key = key;
  }

  public void setNameAsId() {
    setId(getName().replaceAll("\\s+", "-").replaceAll(":", "-").replaceAll("-+", "-").toLowerCase());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean hasDescription() {
    return !Strings.isNullOrEmpty(description);
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public boolean hasRedirectURI() {
    return !Strings.isNullOrEmpty(redirectURI);
  }

  public String getRedirectURI() {
    return redirectURI;
  }

  public void setRedirectURI(String redirectURI) {
    this.redirectURI = redirectURI;
  }

  public boolean hasScopes() {
    return scopes != null && !scopes.isEmpty();
  }

  public boolean hasScope(String name) {
    return hasScopes() && scopes.stream().filter(a -> a.getName().equals(name)).findFirst().isPresent();
  }

  @Nullable
  public Scope getScope(String name) {
    if(!hasScopes()) return null;
    Optional<Scope> action = scopes.stream().filter(a -> a.getName().equals(name)).findFirst();
    return action.isPresent() ? action.get() : null;
  }

  public List<Scope> getScopes() {
    return scopes;
  }

  public void setScopes(List<Scope> scopes) {
    this.scopes = scopes;
  }

  /**
   * Add or update a scope action.
   *
   * @param name
   * @param description
   */
  public void addScope(@NotNull String name, String description) {
    Scope scope = getScope(name);
    if(scope == null) {
      if(scopes == null) scopes = Lists.newArrayList();
      scopes.add(new Scope(name, description));
    } else {
      scope.setDescription(description);
    }
  }

  public void removeScope(String name) {
    Scope scope = getScope(name);
    if (scope != null) scopes.remove(scope);
  }

  @Override
  protected Objects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("name", name) //
      .add("key", key).add("redirectURI", redirectURI).add("scopes", scopes == null
        ? null
        : Joiner.on(",").join(scopes.stream().map(Scope::getName).collect(Collectors.toList())));
  }

  /**
   * A OAuth scope allows to qualify the scope of the authorization granted on the application.
   */
  public static class Scope {

    private String name;

    private String description;

    public Scope() {
    }

    public Scope(String name, String description) {
      this.name = name;
      this.description = description;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private Application application;

    private Builder() {
      application = new Application();
    }

    public Builder name(String name) {
      application.setName(name);
      return this;
    }

    public Builder key(String key) {
      application.setKey(key);
      return this;
    }

    public Builder description(String description) {
      application.setDescription(description);
      return this;
    }

    public Builder redirectURI(String redirectURI) {
      application.setRedirectURI(redirectURI);
      return this;
    }

    public Application build() {
      return application;
    }
  }
}
