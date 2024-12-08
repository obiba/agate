/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import com.google.common.collect.Lists;
import org.obiba.agate.config.Constants;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.Group;
import org.obiba.agate.domain.User;
import org.obiba.agate.security.Roles;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;

@Component
public class UserGroupSeed implements ApplicationListener<ContextRefreshedEvent> {

  private final UserService userService;

  private final GroupService groupService;

  private final Environment env;

  private final ConfigurationService configurationService;

  @Inject
  public UserGroupSeed(UserService userService, GroupService groupService, Environment env, ConfigurationService configurationService) {
    this.userService = userService;
    this.groupService = groupService;
    this.env = env;
    this.configurationService = configurationService;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    seedGroups();
    if (isDevProfile()) {
      seedUsers();
      seedConfiguration();
    }
  }

  private boolean isDevProfile() {
    return Lists.newArrayList(env.getActiveProfiles()).contains(Constants.SPRING_PROFILE_DEVELOPMENT);
  }

  private void seedConfiguration() {
    Configuration config = configurationService.getConfiguration();
    // convenient setting for having a issuer ID in OAuth responses
    if (!config.hasPublicUrl()) config.setPublicUrl(configurationService.getPublicUrl());
    configurationService.save(config);
  }

  private void seedGroups() {
    Configuration config = configurationService.getConfiguration();
    if (!isDevProfile() && config.isGroupsSeeded()) return;
    save(Group.newBuilder().name("mica-administrator").description("Administrate Mica").applications("mica").build());
    save(Group.newBuilder().name("mica-reviewer").description("Edit and publish any Mica content")
      .applications("opal", "mica").build());
    save(Group.newBuilder().name("mica-editor").description("Edit any Mica content").applications("opal", "mica")
      .build());
    save(Group.newBuilder().name("mica-data-access-officer").description("Manage data access requests in Mica")
      .applications("mica").build());
    save(Group.newBuilder().name("mica-user").description("Can submit data access requests in Mica").applications("mica").build());
    save(Group.newBuilder().name("opal-administrator").description("Administrate Opal").applications("opal").build());
    config.setGroupsSeeded(true);
    configurationService.save(config);
  }

  private void save(Group group) {
    if(groupService.findGroup(group.getName()) == null) {
      groupService.save(group);
    }
  }

  private void seedUsers() {
    User.Builder builder;

    builder = User.newBuilder() //
      .name("mica-admin") //
      .firstName("Mica") //
      .lastName("Administrator") //
      .email("mica@example.org") //
      .groups("mica-administrator");

    save(builder.build());

    builder = User.newBuilder() //
      .name("opal-admin") //
      .firstName("Opal") //
      .lastName("Administrator") //
      .email("opal@example.org") //
      .groups("opal-administrator");

    save(builder.build());

    builder = User.newBuilder() //
      .name("agate-admin") //
      .firstName("Agate") //
      .lastName("Administrator") //
      .email("agate@example.org") //
      .role(Roles.AGATE_ADMIN);

    save(builder.build());

    builder = User.newBuilder() //
      .name("super-admin") //
      .firstName("Super") //
      .lastName("Administrator") //
      .email("super@example.org") //
      .role(Roles.AGATE_ADMIN) //
      .groups("opal-administrator", "mica-administrator");

    save(builder.build());

    builder = User.newBuilder() //
      .name("editor") //
      .firstName("Julie") //
      .email("editor@example.org") //
      .groups("mica-editor");

    save(builder.build());

    builder = User.newBuilder() //
      .name("reviewer") //
      .firstName("Christine") //
      .email("reviewer@example.org") //
      .groups("mica-reviewer");

    save(builder.build());

    builder = User.newBuilder() //
      .name("dao") //
      .firstName("Roger") //
      .lastName("Federer") //
      .email("roger.federer@example.org") //
      .role(Roles.AGATE_ADMIN) //
      .groups("mica-data-access-officer");

    save(builder.build());

    builder = User.newBuilder() //
      .name("user1") //
      .firstName("Johnny B.") //
      .lastName("Good") //
      .email("johny.good@example.com") //
      .groups("mica-user");

    save(builder.build());

    builder = User.newBuilder() //
      .name("user2") //
      .inactive() //
      .firstName("Michel") //
      .lastName("Tremblay") //
      .email("user2@example.org") //
      .groups("group1", "group3") //
      .applications("mica");

    save(builder.build());

    builder = User.newBuilder() //
      .name("user3") //
      .pending() //
      .firstName("Monsieur") //
      .lastName("Patate") //
      .email("user3@example.org") //
      .with("institution", "Friterie") //
      .with("note", "Mr Burger's friend");

    save(builder.build());
  }

  private void save(User user) {
    if(userService.findUser(user.getName()) == null) {
      userService.save(user);
      try {
        userService.updateUserPassword(user, "Pa$$w0rd");
      } catch(PasswordNotChangedException e) {
        // ignored
      }
    }
  }
}
