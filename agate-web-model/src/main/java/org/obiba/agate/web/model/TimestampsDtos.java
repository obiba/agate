package org.obiba.agate.web.model;

import org.obiba.mongodb.domain.Timestamped;

class TimestampsDtos {

  private TimestampsDtos() {}

  static Agate.TimestampsDto asDto(Timestamped timestamped) {
    Agate.TimestampsDto.Builder builder = Agate.TimestampsDto.newBuilder()
        .setCreated(timestamped.getCreatedDate().toString());
    if(timestamped.getLastModifiedDate() != null) builder.setLastUpdate(timestamped.getLastModifiedDate().toString());
    return builder.build();
  }

}
