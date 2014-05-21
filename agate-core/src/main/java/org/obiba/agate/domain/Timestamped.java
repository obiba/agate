package org.obiba.agate.domain;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;

public interface Timestamped {

  @NotNull
  DateTime getCreatedDate();

  DateTime getLastModifiedDate();

}
