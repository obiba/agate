package org.obiba.agate.web.model;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.Application;
import org.obiba.agate.domain.Authorization;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.Group;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.TicketService;
import org.obiba.web.model.AuthDtos;
import org.springframework.stereotype.Component;

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
  private ApplicationDtos applicationDtos;

  @Inject
  private GroupDtos groupDtos;

  @Inject
  private ConfigurationDtos configurationDtos;

  @NotNull
  public Agate.TicketDto asDto(@NotNull Ticket ticket) {
    return ticketDtos.asDto(ticket);
  }

  @NotNull
  public Agate.AuthorizationDto asDto(@NotNull Authorization authorization) {
    return ticketDtos.asDto(authorization);
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
    return groupDtos.asDto(group);
  }

  @NotNull
  public Agate.ApplicationDto asDto(@NotNull Application application) {
    return applicationDtos.asDto(application, false);
  }

  @NotNull
  public Agate.ApplicationDto asDto(@NotNull Application application, boolean summary) {
    return applicationDtos.asDto(application, summary);
  }

  @NotNull
  public Agate.ConfigurationDto asDto(@NotNull Configuration configuration) {
    return configurationDtos.asDto(configuration);
  }

  @NotNull
  public Configuration fromDto(@NotNull Agate.ConfigurationDtoOrBuilder dto) {
    return configurationDtos.fromDto(dto);
  }

  @NotNull
  public Application fromDto(@NotNull Agate.ApplicationDto dto) {
    return applicationDtos.fromDto(dto);
  }

  @NotNull
  public Group fromDto(@NotNull Agate.GroupDto dto) {
    return groupDtos.fromDto(dto);
  }
  @NotNull
  public User fromDto(@NotNull Agate.UserDto dto) {
    return userDtos.fromDto(dto);
  }

}
