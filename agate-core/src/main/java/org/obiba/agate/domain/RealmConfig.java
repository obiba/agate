package org.obiba.agate.domain;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Map;
import org.obiba.mongodb.domain.AbstractAuditableDocument;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Document
public class RealmConfig extends AbstractAuditableDocument {

  @NotNull
  @Indexed(unique = true)
  private String name;

  private LocalizedString title;

  private LocalizedString description;

  private RealmStatus status = RealmStatus.INACTIVE;

  private Set<String> groups = Sets.newHashSet();

  private AgateRealm type = AgateRealm.AGATE_USER_REALM;

  private boolean forSignup = false;

  private String domain;

  private String publicUrl;

  private String content;

  private Map<String, String> userInfoMapping = new HashMap<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNameAsId() {
    setId(getName().replaceAll("\\s+", "+").toLowerCase());
  }

  public LocalizedString getTitle() {
    return title;
  }

  public void setTitle(LocalizedString title) {
    this.title = title;
  }

  public LocalizedString getDescription() {
    return description;
  }

  public void setDescription(LocalizedString description) {
    this.description = description;
  }

  public RealmStatus getStatus() {
    return status;
  }

  public void setStatus(RealmStatus status) {
    this.status = status;
  }

  public Set<String> getGroups() {
    return Collections.unmodifiableSet(groups);
  }

  public void setGroups(Collection<String> groups) {
    setGroups(Sets.newHashSet(groups));
  }

  public void setGroups(Set<String> groups) {
    this.groups = groups == null ? Sets.newHashSet() : groups;
  }

  public AgateRealm getType() {
    return type;
  }

  public void setType(AgateRealm type) {
    this.type = type;
  }

  public boolean isForSignup() {
    return forSignup;
  }

  public void setForSignup(boolean forSignup) {
    this.forSignup = forSignup;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public boolean hasDomain() {
    return !Strings.isNullOrEmpty(domain);
  }

  public void setPublicUrl(String publicUrl) {
    this.publicUrl = publicUrl;
  }

  public boolean hasPublicUrl() {
    return !Strings.isNullOrEmpty(publicUrl);
  }

  public String getPublicUrl() {
    if (!Strings.isNullOrEmpty(publicUrl) && publicUrl.endsWith("/")) {
      return publicUrl.substring(0, publicUrl.length() -1);
    }
    return publicUrl;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Map<String, String> getUserInfoMapping() {
    return userInfoMapping;
  }

  public void setUserInfoMapping(Map<String, String> userInfoMapping) {
    this.userInfoMapping = userInfoMapping;
  }

  public static Builder newBuilder() {
    return new Builder();
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
      config.setName(value);
      return this;
    }

    public Builder title(LocalizedString value) {
      config.setTitle(value);
      return this;
    }

    public Builder description(LocalizedString value) {
      config.setDescription(value);
      return this;
    }

    public Builder type(AgateRealm value) {
      config.setType(value);
      return this;
    }

    public Builder forSignup(boolean value) {
      config.setForSignup(value);
      return this;
    }

    public Builder status(RealmStatus value) {
      config.setStatus(value);
      return this;
    }

    public Builder groups(Collection<String> groups) {
      config.setGroups(groups);
      return this;
    }

    public Builder publicUrl(String value) {
      config.setPublicUrl(value);
      return this;
    }

    public Builder domain(String value) {
      config.setDomain(value);
      return this;
    }

    public Builder content(String value) {
      config.setContent(value);
      return this;
    }

    public Builder mapping(Map<String, String> mapping) {
      if (mapping != null) {
        config.setUserInfoMapping(mapping);
      }
      return this;
    }

    public RealmConfig build() {
      return config;
    }
  }
}
