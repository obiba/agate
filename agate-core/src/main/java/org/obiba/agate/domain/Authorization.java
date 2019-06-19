/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.domain;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.mongodb.domain.AbstractAuditableDocument;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * OAuth2 Authorization: specific to a user and an application, has a timeout and can be revoked anytime by the user.
 */
@Document
public class Authorization extends AbstractAuditableDocument {

  @Indexed
  private String username;

  @Indexed
  private String application;

  @NotNull
  @Indexed(unique = true)
  private String code;

  private Set<String> scopes;

  private String redirectURI;

  public Authorization() {}

  public Authorization(String username, String application) {
    this.username = username;
    this.application = application;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public boolean hasScopes() {
    return scopes != null && !scopes.isEmpty();
  }

  public boolean hasScope(String scope) {
    return hasScopes() && scopes.contains(scope);
  }

  public Set<String> getScopes() {
    return scopes;
  }

  public void setScopes(Set<String> scopes) {
    this.scopes = scopes;
  }

  public void addScopes(Set<String> scopes) {
    if (this.scopes == null)
      setScopes(scopes);
    else
      this.scopes.addAll(scopes);
  }

  public String getRedirectURI() {
    return redirectURI;
  }

  public void setRedirectURI(String redirectURI) {
    this.redirectURI = redirectURI;
  }
}
