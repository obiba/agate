/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.domain;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.mongodb.domain.AbstractAuditableDocument;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

/**
 * A group.
 */
@Document(collection = "userGroup") // group is a reserved word in mongodb
public class Group extends AbstractAuditableDocument {

  private static final long serialVersionUID = -2028848270265682755L;

  @NotNull
  @Indexed(unique = true)
  private String name;

  private String description;

  private Set<String> applications = Sets.newHashSet();

  public Group() {
  }

  public Group(@NotNull String name) {
    this(name, null);
  }

  public Group(@NotNull String name, @Nullable String description) {
    this.name = name;
    this.description = description;
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

  public boolean hasDescription() {
    return !Strings.isNullOrEmpty(description);
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) {
      return true;
    }
    if(o == null || getClass() != o.getClass()) {
      return false;
    }

    Group group = (Group) o;

    if(!name.equals(group.name)) {
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
        .add("description", description);
  }

  public Set<String> getApplications() {
    return applications;
  }

  public void setApplications(Set<String> applications) {
    this.applications = applications;
  }

  public boolean hasApplication(String application) {
    return applications.contains(application);
  }

  public void addApplication(String application) {
    applications.add(application);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private Group group;

    private Builder() {
      group = new Group();
    }

    public Builder name(String name) {
      group.setName(name);
      return this;
    }

    public Builder description(String description) {
      group.setDescription(description);
      return this;
    }

    public Builder applications(String... applications) {
      group.setApplications(Sets.newHashSet(applications));
      return this;
    }

    public Builder applications(List<String> applications) {
      group.setApplications(Sets.newHashSet(applications));
      return this;
    }

    public Group build() {
      return group;
    }
  }
}
