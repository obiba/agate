/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.model;

import com.google.common.base.Strings;
import jakarta.annotation.Nonnull;
import org.obiba.agate.domain.Application;
import org.obiba.agate.domain.Authorization;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.service.ApplicationService;
import org.obiba.agate.service.AuthorizationService;
import org.obiba.agate.service.TicketService;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;

@Component
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class TicketDtos {

  @Inject
  private TicketService ticketService;

  @Inject
  private AuthorizationService authorizationService;

  @Inject
  private ApplicationService applicationService;

  @Nonnull
  Agate.TicketDto asDto(@Nonnull Ticket ticket) {
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

  @Nonnull
  Agate.AuthorizationDto asDto(@Nonnull Authorization authorization) {
    Agate.AuthorizationDto.Builder builder = Agate.AuthorizationDto.newBuilder();

    Application application = applicationService.find(authorization.getApplication());

    builder.setId(authorization.getId()) //
      .setUsername(authorization.getUsername()) //
      .setApplication(authorization.getApplication()) //
      .setApplicationName(application == null ? authorization.getApplication() : application.getName()) //
      .setCode(authorization.getCode()) //
      .setRedirectURI(authorization.getRedirectURI()) //
      .setTimestamps(TimestampsDtos.asDto(authorization));

    if(authorization.hasScopes()) builder.addAllScopes(authorization.getScopes());

    return builder.build();
  }

}
