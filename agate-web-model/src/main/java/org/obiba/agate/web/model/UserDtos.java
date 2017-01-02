/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.model;

import java.util.Optional;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.UserService;
import org.obiba.web.model.AuthDtos;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class UserDtos {

  @Inject
  UserService userService;

  @NotNull
  Agate.UserDto asDto(@NotNull User user) {
    Agate.UserDto.Builder builder = Agate.UserDto.newBuilder();
    builder.setId(user.getId()) //
      .setName(user.getName()) //
      .setRealm(user.getRealm()) //
      .setRole(user.getRole())//
      .setStatus(user.getStatus().toString()) //
      .setTimestamps(TimestampsDtos.asDto(user))
      .setPreferredLanguage(user.getPreferredLanguage());

    if(!Strings.isNullOrEmpty(user.getFirstName())) builder.setFirstName(user.getFirstName());
    if(!Strings.isNullOrEmpty(user.getLastName())) builder.setLastName(user.getLastName());
    if(!Strings.isNullOrEmpty(user.getEmail())) builder.setEmail(user.getEmail());
    if(user.hasAttributes()) user.getAttributes()
      .forEach((n, v) -> builder.addAttributes(Agate.AttributeDto.newBuilder().setName(n).setValue(v)));

    if(user.hasGroups()) {
      builder.addAllGroups(user.getGroups());

      user.getGroups().forEach(g -> Optional.ofNullable(userService.findGroup(g)).flatMap(r -> {
          r.getApplications().forEach(
            a -> builder.addGroupApplications(Agate.GroupApplicationDto.newBuilder().setGroup(g).setApplication(a))
              .build());
          return Optional.of(r);
        }));
    }

    if(user.hasApplications()) builder.addAllApplications(user.getApplications());

    if (user.getLastLogin() != null) builder.setLastLogin(user.getLastLogin().toString());

    return builder.build();
  }

  AuthDtos.SubjectDto asDto(@NotNull User user, boolean withAttributes) {
    AuthDtos.SubjectDto.Builder builder = AuthDtos.SubjectDto.newBuilder().setUsername(user.getName());
    if(!user.getGroups().isEmpty()) builder.addAllGroups(user.getGroups());

    if(withAttributes) {
      addAttribute(builder, "firstName", user.getFirstName());
      addAttribute(builder, "lastName", user.getLastName());
      addAttribute(builder, "email", user.getEmail());
      addAttribute(builder, "locale", user.getPreferredLanguage());
      addAttribute(builder, "createdDate", user.getCreatedDate().toString());
      DateTime lastLogin = user.getLastLogin();
      if (lastLogin != null) addAttribute(builder, "lastLogin", lastLogin.toString());
      user.getAttributes().forEach((n, v) -> addAttribute(builder, n, v));
    }

    return builder.build();
  }

  @NotNull
  User fromDto(@NotNull Agate.UserDto dto) {
    User.Builder builder = User.newBuilder()
      .name(dto.getName())
      .realm(dto.getRealm())
      .role(dto.getRole())
      .status(dto.getStatus());

    if (dto.hasPreferredLanguage()) {
      builder.preferredLanguage(dto.getPreferredLanguage());
    }

    if (dto.hasId()) {
      builder.id(dto.getId());
    }

    if (dto.hasFirstName()) {
      builder.firstName(dto.getFirstName());
    }

    if (dto.hasLastName()) {
      builder.lastName(dto.getLastName());
    }

    if (dto.hasEmail()) {
      builder.email(dto.getEmail());
    }

    if (dto.getGroupsCount() > 0) {
      builder.groups(dto.getGroupsList());
    }

    if (dto.getApplicationsCount() > 0) {
      builder.applications(dto.getApplicationsList());
    }

    if (dto.getAttributesCount() > 0) {
      dto.getAttributesList().forEach(att -> builder.attribute(att.getName(), att.getValue()));
    }

    return builder.build();
  }

  private void addAttribute(AuthDtos.SubjectDto.Builder builder, String key, String value) {
    builder
      .addAttributes(AuthDtos.SubjectDto.AttributeDto.newBuilder().setKey(key).setValue(value == null ? "" : value));
  }

}
