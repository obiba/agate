package org.obiba.agate.event;

import org.springframework.data.domain.Persistable;

public class RealmConfigDeactivatedEvent extends PersistableDeletedEvent {

  public RealmConfigDeactivatedEvent(Persistable persistable) {
    super(persistable);
  }
}
