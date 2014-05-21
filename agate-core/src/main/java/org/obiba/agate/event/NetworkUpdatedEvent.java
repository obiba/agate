package org.obiba.agate.event;

import org.obiba.agate.domain.Network;

public class NetworkUpdatedEvent extends PersistableUpdatedEvent {

  public NetworkUpdatedEvent(Network network) {
    super(network);
  }
}
