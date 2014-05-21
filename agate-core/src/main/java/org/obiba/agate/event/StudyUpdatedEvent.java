package org.obiba.agate.event;

import org.obiba.agate.domain.Study;

public class StudyUpdatedEvent extends PersistableUpdatedEvent {

  public StudyUpdatedEvent(Study study) {
    super(study);
  }
}
