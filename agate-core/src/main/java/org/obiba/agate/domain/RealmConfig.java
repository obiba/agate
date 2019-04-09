package org.obiba.agate.domain;

import com.google.common.collect.Sets;
import org.obiba.mongodb.domain.AbstractAuditableDocument;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Document
public class RealmConfig extends AbstractAuditableDocument {

  @NotNull
  @Indexed(unique = true)
  private String name;

  private String title;

  private String description;

  private RealmStatus status;

  private Set<String> groups = Sets.newHashSet();

  private AgateRealm realm = AgateRealm.USER_REALM;

  private boolean defaultRealm = false;

  private boolean forSignup = false;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public RealmStatus getStatus() {
    return status;
  }

  public void setStatus(RealmStatus status) {
    this.status = status;
  }

  public Set<String> getGroups() {
    return groups;
  }

  public boolean hasGroups() {
    return groups != null && !groups.isEmpty();
  }

  public void setGroups(List<String> groups) {
    setGroups(Sets.newHashSet(groups));
  }

  public void setGroups(Set<String> groups) {
    this.groups = groups;
  }

  public AgateRealm getRealm() {
    return realm;
  }

  public void setRealm(AgateRealm realm) {
    this.realm = realm;
  }

  public boolean isDefaultRealm() {
    return defaultRealm;
  }

  public void setDefaultRealm(boolean defaultRealm) {
    this.defaultRealm = defaultRealm;
  }

  public boolean isForSignup() {
    return forSignup;
  }

  public void setForSignup(boolean forSignup) {
    this.forSignup = forSignup;
  }
}
