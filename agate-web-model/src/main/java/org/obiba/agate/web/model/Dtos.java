package org.obiba.agate.web.model;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.AgateConfig;
import org.obiba.agate.domain.Group;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.TicketService;
import org.obiba.web.model.AuthDtos;
import org.springframework.stereotype.Component;

import static org.obiba.agate.web.model.Agate.AgateConfigDto;
import static org.obiba.agate.web.model.Agate.AgateConfigDtoOrBuilder;

@Component
@SuppressWarnings("OverlyCoupledClass")
public class Dtos {

  @Inject
  private TicketService ticketService;

  @Inject
  private TicketDtos ticketDtos;

  @Inject
  private UserDtos userDtos;

  @Inject
  private AgateConfigDtos agateConfigDtos;

  @NotNull
  public Agate.TicketDto asDto(@NotNull Ticket ticket) {
    return ticketDtos.asDto(ticket);
  }

  @NotNull
  public Agate.UserDto asDto(@NotNull User user) {
    return userDtos.asDto(user);
  }

  @NotNull
  public AuthDtos.SubjectDto asDto(@NotNull User user, boolean withAttributes) {
    return userDtos.asDto(user, withAttributes);
  }

  @NotNull
  public Agate.GroupDto asDto(@NotNull Group group) {
    return userDtos.asDto(group);
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
