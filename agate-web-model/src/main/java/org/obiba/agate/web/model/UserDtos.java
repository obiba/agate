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

import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.obiba.agate.domain.AttributeConfiguration;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.model.translation.JsonTranslator;
import org.obiba.agate.web.model.translation.Translator;
import org.obiba.web.model.AuthDtos;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class UserDtos {

  @Inject
  UserService userService;
  @Inject
  ConfigurationService configurationService;

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
    return asDto(user, withAttributes, toTranslate -> toTranslate);
  }

  AuthDtos.SubjectDto asDto(@NotNull User user, boolean withAttributes, @NotNull String language) {
    return asDto(user, withAttributes, findTranslatorForLanguage(language));
  }

  AuthDtos.SubjectDto asDto(@NotNull User user, boolean withAttributes, @NotNull Translator translator) {

    AuthDtos.SubjectDto.Builder builder = AuthDtos.SubjectDto.newBuilder().setUsername(user.getName());
    if (!user.getGroups().isEmpty()) builder.addAllGroups(user.getGroups());

    if (withAttributes) {
      addAttribute(builder, translator.translate("firstName"), user.getFirstName());
      addAttribute(builder, translator.translate("lastName"), user.getLastName());
      addAttribute(builder, translator.translate("email"), user.getEmail());
      addAttribute(builder, translator.translate("locale"), user.getPreferredLanguage());
      addAttribute(builder, translator.translate("createdDate"), user.getCreatedDate().toString());
      DateTime lastLogin = user.getLastLogin();
      if (lastLogin != null) addAttribute(builder, translator.translate("lastLogin"), lastLogin.toString());
      user.getAttributes().entrySet().stream()
        .filter(isGenericAttribute())
        .map(translateConfiguredValues(translator))
        .collect(Collectors.toList())
        .forEach((entry) -> addAttribute(builder, translator.translate(entry.getKey()), entry.getValue()));
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

  private Function<Map.Entry<String, String>, Map.Entry<String, String>> translateConfiguredValues(Translator translator) {

    List<AttributeConfiguration> userAttributes = configurationService.getConfiguration().getUserAttributes();

    return entry -> {
      userAttributes.stream()
        .filter(t -> t.getName().equals(entry.getKey()))
        .filter(t -> isBoolean(t)
          || (t.getType().equals(AttributeConfiguration.Type.STRING) && !CollectionUtils.isEmpty(t.getValues())))
        .findFirst().ifPresent(t -> entry.setValue(translator.translate(entry.getValue())));

      return entry;
    };
  }

  private boolean isBoolean(AttributeConfiguration attributeConfiguration) {
    return attributeConfiguration.getType().equals(AttributeConfiguration.Type.BOOLEAN);
  }

  private Predicate<Map.Entry<String, String>> isGenericAttribute() {

    List<String> globalAttributeNames = configurationService.getConfiguration()
      .getUserAttributes().stream()
      .map(AttributeConfiguration::getName)
      .collect(Collectors.toList());

    return entry -> globalAttributeNames.contains(entry.getKey());
  }

  private Translator findTranslatorForLanguage(String language){
    try {
      return JsonTranslator.getTranslatorFor(configurationService.getTranslationDocument(language));
    } catch (IOException e) {
      return toTranslate -> toTranslate;
    }
  }
}
