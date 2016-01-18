package org.obiba.agate.web.model;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.Authorization;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.service.AuthorizationService;
import org.obiba.agate.service.TicketService;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class TicketDtos {

  @Inject
  private TicketService ticketService;

  @Inject
  private AuthorizationService authorizationService;

  @NotNull
  Agate.TicketDto asDto(@NotNull Ticket ticket) {
    Agate.TicketDto.Builder builder = Agate.TicketDto.newBuilder();
    builder.setId(ticket.getId()) //
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

    if(ticket.hasAuthorization()) {
      Authorization authorization = authorizationService.find(ticket.getAuthorization());
      if(authorization != null) builder.setAuthorization(asDto(authorization));
    }

    return builder.build();
  }

  @NotNull
  Agate.AuthorizationDto asDto(@NotNull Authorization authorization) {
    Agate.AuthorizationDto.Builder builder = Agate.AuthorizationDto.newBuilder();
    builder.setId(authorization.getId()) //
      .setUsername(authorization.getUsername()) //
      .setApplication(authorization.getApplication()) //
      .setCode(authorization.getCode()) //
      .setRedirectURI(authorization.getRedirectURI()) //
      .setTimestamps(TimestampsDtos.asDto(authorization));

    if(authorization.hasScopes()) builder.addAllScopes(authorization.getScopes());

    return builder.build();
  }

}
