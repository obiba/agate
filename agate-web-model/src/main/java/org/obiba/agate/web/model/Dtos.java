package org.obiba.agate.web.model;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.AgateConfig;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.service.TicketService;
import org.springframework.stereotype.Component;

import static org.obiba.agate.web.model.Agate.AgateConfigDto;
import static org.obiba.agate.web.model.Agate.AgateConfigDtoOrBuilder;

@Component
@SuppressWarnings("OverlyCoupledClass")
public class Dtos {

  @Inject
  private TicketService ticketService;

  @Inject
  private GrantingTicketDtos grantingTicketDtos;

  @Inject
  private AgateConfigDtos agateConfigDtos;

  @NotNull
  public Agate.GrantingTicketDto asDto(@NotNull Ticket ticket) {
    return grantingTicketDtos.asDto(ticket);
  }

  @NotNull
  public AgateConfigDto asDto(@NotNull AgateConfig agateConfig) {
    return agateConfigDtos.asDto(agateConfig);
  }

  @NotNull
  public AgateConfig fromDto(@NotNull AgateConfigDtoOrBuilder dto) {
    return agateConfigDtos.fromDto(dto);
  }

}
