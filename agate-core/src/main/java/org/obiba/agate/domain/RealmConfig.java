package org.obiba.agate.domain;

import com.google.common.collect.Sets;
import net.minidev.json.annotate.JsonIgnore;
import org.obiba.mongodb.domain.AbstractAuditableDocument;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Document
public class RealmConfig extends AbstractAuditableDocument {

  @NotNull
  @Indexed(unique = true)
  private String name;

  private String title;

  private String description;

  private RealmStatus status = RealmStatus.INACTIVE;

  private Set<String> groups = Sets.newHashSet();

  private AgateRealm realm = AgateRealm.AGATE_USER_REALM;

  private boolean defaultRealm = false;

  private boolean forSignup = false;

  public String getName() {
    return name;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public RealmStatus getStatus() {
    return status;
  }

  public Set<String> getGroups() {
    return groups;
  }

  public boolean hasGroups() {
    return groups != null && !groups.isEmpty();
  }

  public AgateRealm getRealm() {
    return realm;
  }

  public boolean isDefaultRealm() {
    return defaultRealm;
  }

  public boolean isForSignup() {
    return forSignup;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static String generateId() {
    return UUID.randomUUID().toString();
  }

  public static void mergePropeties(RealmConfig config, RealmConfig from) {
    BeanUtils.copyProperties(config, from, "id", "name", "createdBy", "createdDate", "lastModifiedBy",
      "lastModifiedDate");
  }

  public static class Builder {

    private RealmConfig config;

    private Builder() {
      config = new RealmConfig();
    }

    public Builder id(String value) {
      config.setId(value);
      return this;
    }

    public Builder name(String value) {
      config.name = value;
      return this;
    }

    public Builder title(String value) {
      config.title = value;
      return this;
    }

    public Builder description(String value) {
      config.description = value;
      return this;
    }

    public Builder realm(String value) {
      return realm(AgateRealm.fromString(value));
    }

    public Builder realm(AgateRealm value) {
      config.realm = value;
      return this;
    }

    public Builder defaultRealm(boolean value) {
      config.defaultRealm = value;
      return this;
    }

    public Builder forSignup(boolean value) {
      config.forSignup = value;
      return this;
    }

    public Builder status(RealmStatus value) {
      config.status = value;
      return this;
    }

    public Builder setGroups(List<String> groups) {
      return setGroups(Sets.newHashSet(groups));
    }

    public Builder setGroups(Set<String> groups) {
      config.groups = groups;
      return this;
    }

    public RealmConfig build() {
      return config;
    }
  }
}
