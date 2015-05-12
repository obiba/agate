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

import javax.validation.constraints.NotNull;

import org.obiba.mongodb.domain.AbstractAuditableDocument;
import org.springframework.data.mongodb.core.index.Indexed;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class Application extends AbstractAuditableDocument {

  private static final long serialVersionUID = 4710884170897922907L;

  @NotNull
  @Indexed(unique = true)
  private String name;

  private String description;

  private String key;

  public Application() {
  }

  public Application(String name, String key) {
    this.name = name;
    this.key = key;
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

  @Override
  protected Objects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("name", name) //
        .add("key", key);
  }
}
