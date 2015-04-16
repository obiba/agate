/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.model;

import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.Application;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class ApplicationDtos {

  @NotNull
  Agate.ApplicationDto asDto(@NotNull Application application) {
    Agate.ApplicationDto.Builder builder = Agate.ApplicationDto.newBuilder();
    builder.setId(application.getId()) //
        .setName(application.getName()) //
        .setKey(application.getKey()) //
        .setTimestamps(TimestampsDtos.asDto(application));

    if(application.hasDescription()) builder.setDescription(application.getDescription());

    return builder.build();
  }

  @NotNull
  Application fromDto(@NotNull Agate.ApplicationDto dto) {
    Application application = new Application(dto.getName(), dto.getKey());

    if (dto.hasDescription()) application.setDescription(dto.getDescription());

    return application;
  }
}
