package org.obiba.agate.event;

import org.obiba.agate.domain.AgateConfig;

public class AgateConfigUpdatedEvent extends PersistableUpdatedEvent {

  public AgateConfigUpdatedEvent(AgateConfig agateConfig) {
    super(agateConfig);
  }
}
