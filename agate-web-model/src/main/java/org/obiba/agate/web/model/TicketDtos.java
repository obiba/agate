package org.obiba.agate.web.model;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.service.ConfigurationService;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class TicketDtos {

  @Inject
  private ConfigurationService configurationService;

  @NotNull
  Agate.TicketDto asDto(@NotNull Ticket ticket) {
    Agate.TicketDto.Builder builder = Agate.TicketDto.newBuilder();
    builder.setId(ticket.getId()) //
        .setUsername(ticket.getUsername()) //
        .setRemembered(ticket.isRemembered()) //
        .setTimestamps(TimestampsDtos.asDto(ticket));

    for(Ticket.Log log : ticket.getLogs()) {
      builder.addLogs(Agate.TicketDto.Log.newBuilder() //
          .setApplication(log.getApplication()) //
          .setAction(log.getAction()) //
          .setTime(log.getTime().toString()));
    }

    Configuration configuration = configurationService.getConfiguration();
    DateTime created = ticket.getCreatedDate();
    DateTime expires = ticket.isRemembered()
        ? created.plusHours(configuration.getLongTimeout())
        : created.plusHours(configuration.getShortTimeout());
    builder.setExpires(expires.toString());

    return builder.build();
  }

}
