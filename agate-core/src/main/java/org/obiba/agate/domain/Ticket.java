/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
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
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Ticket is the token granted to a user for performing single sign-on.
 */
@Document
public class Ticket extends AbstractAuditableDocument {

  private static final long serialVersionUID = -1309201668631219671L;

  // legacy
  private String token;

  @Indexed
  private String username;

  private boolean remembered = false;

  private List<Event> events;

  private String authorization;

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

  public List<Event> getEvents() {
    return events == null ? events = Lists.newArrayList() : events;
  }

  public void setEvents(List<Event> events) {
    this.events = events;
  }

  public void addEvent(String application, String action) {
    getEvents().add(new Event(application, action));
  }

  public boolean hasAuthorization() {
    return !Strings.isNullOrEmpty(authorization);
  }

  public String getAuthorization() {
    return authorization;
  }

  public void setAuthorization(String authorization) {
    this.authorization = authorization;
  }

  @Override
  protected Objects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("username", username) //
        .add("remembered", remembered);
  }

  public static class Event {
    private String application;

    private String action;

    private DateTime time = DateTime.now();

    public Event() {
    }

    public Event(String application, String action) {
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
