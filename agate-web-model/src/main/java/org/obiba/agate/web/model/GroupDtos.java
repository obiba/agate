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

import org.obiba.agate.domain.Group;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class GroupDtos {

  @NotNull
  Agate.GroupDto asDto(@NotNull Group Group) {
    Agate.GroupDto.Builder builder = Agate.GroupDto.newBuilder();
    builder.setId(Group.getId()) //
        .setName(Group.getName()) //
        .setTimestamps(TimestampsDtos.asDto(Group));

    if(Group.hasDescription()) builder.setDescription(Group.getDescription());

    return builder.build();
  }

  @NotNull
  Group fromDto(@NotNull Agate.GroupDto dto) {
    Group Group = new Group(dto.getName());

    if (dto.hasDescription()) Group.setDescription(dto.getDescription());

    return Group;
  }
}
