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

import jakarta.annotation.Nonnull;
import org.obiba.agate.domain.*;
import org.obiba.agate.service.TicketService;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.web.model.AuthDtos;
import org.obiba.web.model.OIDCDtos;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import javax.inject.Inject;

@Component
@SuppressWarnings("OverlyCoupledClass")
public class Dtos {

  private final TicketDtos ticketDtos;

  private final UserDtos userDtos;

  private final ApplicationDtos applicationDtos;

  private final GroupDtos groupDtos;

  private final RealmConfigDtos realmConfigDtos;

  private final ConfigurationDtos configurationDtos;

  private final OidcAuthProviderDtos oidcAuthProviderDtos;

  @Inject
  public Dtos(TicketService ticketService,
              TicketDtos ticketDtos,
              UserDtos userDtos,
              ApplicationDtos applicationDtos,
              GroupDtos groupDtos,
              RealmConfigDtos realmConfigDtos,
              ConfigurationDtos configurationDtos, OidcAuthProviderDtos oidcAuthProviderDtos) {
    this.ticketDtos = ticketDtos;
    this.userDtos = userDtos;
    this.applicationDtos = applicationDtos;
    this.groupDtos = groupDtos;
    this.realmConfigDtos = realmConfigDtos;
    this.configurationDtos = configurationDtos;
    this.oidcAuthProviderDtos = oidcAuthProviderDtos;
  }

  @Nonnull
  public Agate.TicketDto asDto(@Nonnull Ticket ticket) {
    return ticketDtos.asDto(ticket);
  }

  @Nonnull
  public Agate.AuthorizationDto asDto(@Nonnull Authorization authorization) {
    return ticketDtos.asDto(authorization);
  }

  @Nonnull
  public Agate.UserDto asDto(@Nonnull User user) {
    return userDtos.asDto(user);
  }

  @Nonnull
  public Agate.UserSummaryDto asSummaryDto(@Nonnull User user) {
    return userDtos.asSummaryDto(user);
  }

  @Nonnull
  public AuthDtos.SubjectDto asDto(@Nonnull User user, boolean withAttributes) {
    return userDtos.asDto(user, withAttributes);
  }

  @Nonnull
  public Agate.GroupDto.Builder asDtoBuilder(@Nonnull Group group) {
    return groupDtos.asBuilderDto(group);
  }

  @Nonnull
  public Agate.GroupDto asDto(@Nonnull Group group) {
    return asDtoBuilder(group).build();
  }

  @Nonnull
  public Agate.ApplicationDto asDto(@Nonnull Application application) {
    return applicationDtos.asDto(application, false);
  }

  @Nonnull
  public Agate.ApplicationDto asDto(@Nonnull Application application, boolean summary) {
    return applicationDtos.asDto(application, summary);
  }

  @Nonnull
  public Agate.ConfigurationDto asDto(@Nonnull Configuration configuration) {
    return configurationDtos.asDto(configuration);
  }

  @Nonnull
  public Agate.AttributeConfigurationDto asDto(@Nonnull AttributeConfiguration attributeConfiguration) {
    return configurationDtos.asDto(attributeConfiguration).build();
  }

  @Nonnull
  public Configuration fromDto(@Nonnull Agate.ConfigurationDtoOrBuilder dto) {
    return configurationDtos.fromDto(dto);
  }

  @Nonnull
  public Application fromDto(@Nonnull Agate.ApplicationDto dto) {
    return applicationDtos.fromDto(dto);
  }

  @Nonnull
  public Group fromDto(@Nonnull Agate.GroupDto dto) {
    return groupDtos.fromDto(dto);
  }
  @Nonnull
  public User fromDto(@Nonnull Agate.UserDto dto) {
    return userDtos.fromDto(dto);
  }

  @Nonnull
  public RealmConfig fromDto(Agate.RealmConfigDto dto) {
    return realmConfigDtos.fromDto(dto);
  }

  @Nonnull
  public Agate.RealmConfigDto.Builder asDtoBuilder(RealmConfig config) {
    return realmConfigDtos.asDtoBuilder(config);
  }

  @Nonnull
  public Agate.RealmConfigDto asDto(RealmConfig config) {
    return realmConfigDtos.asDtoBuilder(config).build();
  }

  @Nonnull
  public Agate.RealmConfigSummaryDto asSummaryDto(RealmConfig config) {
    return realmConfigDtos.asSummaryDto(config);
  }

  @Nonnull
  public OIDCDtos.OIDCAuthProviderSummaryDto asSummaryDto(@Nonnull OIDCConfiguration configuration, @Nullable String locale) {
    return oidcAuthProviderDtos.asSummaryDto(configuration, locale);
  }
}
