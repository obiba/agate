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

import org.joda.time.DateTime;
import org.obiba.mongodb.domain.AbstractAuditableDocument;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

@Document
public class Ticket extends AbstractAuditableDocument {

  private static final long serialVersionUID = -1309201668631219671L;

  @Indexed
  private String username;

  private boolean remembered = false;

  private List<Log> logs;

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

  public List<Log> getLogs() {
    return logs == null ? logs = Lists.newArrayList() : logs;
  }

  public void setLogs(List<Log> logs) {
    this.logs = logs;
  }

  public void addLog(String application, String action) {
    getLogs().add(new Log(application, action));
  }

  @Override
  protected Objects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("username", username) //
        .add("remembered", remembered);
  }

  public static class Log {
    private String application;

    private String action;

    private DateTime time = DateTime.now();

    public Log() {
    }

    public Log(String application, String action) {
      this.application = application;
      this.action = action;
    }

    public String getApplication() {
      return application;
    }

    public void setApplication(String application) {
      this.application = application;
    }

    public String getAction() {
      return action;
    }

    public void setAction(String action) {
      this.action = action;
    }

    public DateTime getTime() {
      return time;
    }

    public void setTime(DateTime time) {
      this.time = time;
    }
  }

}
