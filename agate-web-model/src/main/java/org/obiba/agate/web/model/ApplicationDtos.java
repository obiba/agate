/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.model;

import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.Application;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class ApplicationDtos {

  @NotNull
  Agate.ApplicationDto asDto(@NotNull Application application, boolean summary) {
    Agate.ApplicationDto.Builder builder = Agate.ApplicationDto.newBuilder();
    builder.setId(application.getId()) //
      .setName(application.getName()) //
      .setTimestamps(TimestampsDtos.asDto(application));

    if(application.hasDescription()) builder.setDescription(application.getDescription());
    if(application.hasRedirectURI() && !summary) builder.setRedirectURI(application.getRedirectURI());

    if(application.hasScopes()) builder.addAllScopes(application.getScopes().stream().map(this::asDto)
      .collect(Collectors.toList()));

    return builder.build();
  }

  private Agate.ApplicationDto.ScopeDto asDto(@NotNull Application.Scope scope) {
    return Agate.ApplicationDto.ScopeDto.newBuilder().setName(scope.getName()).setDescription(scope.getDescription()).build();
  }

  @NotNull
  Application fromDto(@NotNull Agate.ApplicationDto dto) {
    Application application = new Application(dto.getName());

    application.setDescription(dto.getDescription());
    if(dto.hasKey()) application.setKey(dto.getKey());
    application.setRedirectURI(dto.getRedirectURI());
    application.setScopes(dto.getScopesList().stream().map(this::fromDto).collect(Collectors.toList()));

    return application;
  }

  private Application.Scope fromDto(@NotNull Agate.ApplicationDto.ScopeDto dto) {
    return new Application.Scope(dto.getName(), dto.getDescription());
  }
}
