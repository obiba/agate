package org.obiba.agate.web.model;

import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.Ticket;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class TicketDtos {

  @NotNull
  Agate.TicketDto asDto(@NotNull Ticket ticket) {
    Agate.TicketDto.Builder builder = Agate.TicketDto.newBuilder();
    builder.setId(ticket.getId()) //
        .setTimestamps(TimestampsDtos.asDto(ticket));
    return builder.build();
  }

}
