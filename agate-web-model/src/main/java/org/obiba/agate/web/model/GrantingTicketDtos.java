package org.obiba.agate.web.model;

import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.Ticket;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class GrantingTicketDtos {

  @NotNull
  Agate.GrantingTicketDto asDto(@NotNull Ticket ticket) {
    Agate.GrantingTicketDto.Builder builder = Agate.GrantingTicketDto.newBuilder();
    builder.setId(ticket.getId()) //
        .setTimestamps(TimestampsDtos.asDto(ticket));
    return builder.build();
  }

}
