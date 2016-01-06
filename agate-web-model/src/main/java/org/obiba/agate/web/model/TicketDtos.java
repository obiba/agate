package org.obiba.agate.web.model;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.Ticket;
import org.obiba.agate.service.TicketService;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class TicketDtos {

  @Inject
  private TicketService ticketService;

  @NotNull
  Agate.TicketDto asDto(@NotNull Ticket ticket) {
    Agate.TicketDto.Builder builder = Agate.TicketDto.newBuilder();
    builder.setId(ticket.getId()) //
      .setToken(ticket.getToken()) //
      .setUsername(ticket.getUsername()) //
      .setRemembered(ticket.isRemembered()) //
      .setTimestamps(TimestampsDtos.asDto(ticket));

    for(Ticket.Event event : ticket.getEvents()) {
      Agate.TicketDto.Event.Builder eBuilder = Agate.TicketDto.Event.newBuilder() //

        .setAction(event.getAction()) //
        .setTime(event.getTime().toString());
      if(!Strings.isNullOrEmpty(event.getApplication())) eBuilder.setApplication(event.getApplication());
      builder.addEvents(eBuilder);
    }

    builder.setExpires(ticketService.getExpirationDate(ticket).toString());

    return builder.build();
  }

}
