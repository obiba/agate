package org.obiba.agate.event;

import org.springframework.data.domain.Persistable;

public class RealmConfigActivatedOrUpdatedEvent extends PersistableUpdatedEvent {

  public RealmConfigActivatedOrUpdatedEvent(Persistable persistable) {
    super(persistable);
  }
}
