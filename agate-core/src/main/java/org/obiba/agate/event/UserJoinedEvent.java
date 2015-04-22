package org.obiba.agate.event;

import org.obiba.agate.domain.User;

public class UserJoinedEvent extends PersistableUpdatedEvent<User>{
  public UserJoinedEvent(User user) {
    super(user);
  }
}
