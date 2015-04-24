package org.obiba.agate.event;

import org.obiba.agate.domain.User;

public class UserApprovedEvent extends PersistableUpdatedEvent<User> {
  public UserApprovedEvent(User user) {
    super(user);
  }
}
