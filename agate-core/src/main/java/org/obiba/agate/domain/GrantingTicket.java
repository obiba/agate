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
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.base.Objects;

@Document
public class GrantingTicket extends AbstractAuditableDocument {

  private static final long serialVersionUID = -1309201668631219671L;

  public static final String CAS_PREFIX = "TGT-";

  @Indexed
  private String username;

  private boolean remembered = false;

  public String getCASId() {
    return CAS_PREFIX + getId();
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public boolean isRemembered() {
    return remembered;
  }

  public void setRemembered(boolean remembered) {
    this.remembered = remembered;
  }

  /**
   * Remove the CAS prefix if any.
   * @param id
   * @return
   */
  public static String toId(@NotNull String id) {
    String gtId = id;
    if (id.startsWith(CAS_PREFIX)) {
      gtId = id.substring(CAS_PREFIX.length());
    }
    return gtId;
  }

  @Override
  protected Objects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("username", username) //
        .add("remembered", remembered);
  }

}
