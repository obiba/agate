package org.obiba.agate.event;

import org.obiba.agate.domain.Configuration;

public class AgateConfigUpdatedEvent extends PersistableUpdatedEvent {

  public AgateConfigUpdatedEvent(Configuration configuration) {
    super(configuration);
  }
}
