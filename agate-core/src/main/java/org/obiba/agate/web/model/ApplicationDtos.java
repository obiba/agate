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
import org.obiba.agate.domain.Application;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class ApplicationDtos {

  @Nonnull
  Agate.ApplicationDto asDto(@Nonnull Application application, boolean summary) {
    Agate.ApplicationDto.Builder builder = Agate.ApplicationDto.newBuilder();
    builder.setId(application.getId()) //
      .setName(application.getName()) //
      .setTimestamps(TimestampsDtos.asDto(application));

    if(application.hasDescription()) builder.setDescription(application.getDescription());
    if(application.hasRedirectURI() && !summary) builder.setRedirectURI(application.getRedirectURI());

    if(application.hasScopes()) builder.addAllScopes(application.getScopes().stream().map(this::asDto)
      .collect(Collectors.toList()));

    builder.setAutoApproval(application.isAutoApproval());

    return builder.build();
  }

  private Agate.ApplicationDto.ScopeDto asDto(@Nonnull Application.Scope scope) {
    return Agate.ApplicationDto.ScopeDto.newBuilder().setName(scope.getName()).setDescription(scope.getDescription()).build();
  }

  @Nonnull
  Application fromDto(@Nonnull Agate.ApplicationDto dto) {
    Application application = new Application(dto.getName());

    application.setDescription(dto.getDescription());
    if(dto.hasKey()) application.setKey(dto.getKey());
    application.setRedirectURI(dto.getRedirectURI());
    application.setScopes(dto.getScopesList().stream().map(this::fromDto).collect(Collectors.toList()));

    if (dto.hasAutoApproval()) application.setAutoApproval(dto.getAutoApproval());

    return application;
  }

  private Application.Scope fromDto(@Nonnull Agate.ApplicationDto.ScopeDto dto) {
    return new Application.Scope(dto.getName(), dto.getDescription());
  }
}
