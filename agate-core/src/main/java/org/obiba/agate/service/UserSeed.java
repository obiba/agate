/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import javax.inject.Inject;

import org.obiba.agate.config.Profiles;
import org.obiba.agate.domain.User;
import org.obiba.agate.security.Roles;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Profile(Profiles.DEV)
public class UserSeed implements ApplicationListener<ContextRefreshedEvent> {

  @Inject
  private UserService userService;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    User.Builder builder;

    builder = User.newBuilder() //
        .name("mica-admin") //
        .password(userService.hashPassword("password")) //
        .firstName("Mica") //
        .lastName("Administrator") //
        .email("mica@example.org") //
        .groups("mica-administrator");

    save(builder.build());

    builder = User.newBuilder() //
        .name("opal-admin") //
        .password(userService.hashPassword("password")) //
        .firstName("Opal") //
        .lastName("Administrator") //
        .email("opal@example.org") //
        .groups("opal-administrator");

    save(builder.build());

    builder = User.newBuilder() //
        .name("agate-admin") //
        .password(userService.hashPassword("password")) //
        .firstName("Agate") //
        .lastName("Administrator") //
        .email("agate@example.org") //
        .role(Roles.AGATE_ADMIN);

    save(builder.build());

    builder = User.newBuilder() //
        .name("super-admin") //
        .password(userService.hashPassword("password")) //
        .firstName("Super") //
        .lastName("Administrator") //
        .email("super@example.org") //
        .role(Roles.AGATE_ADMIN) //
        .groups("opal-administrator", "mica-administrator");

    save(builder.build());

    builder = User.newBuilder() //
        .name("anonymous") //
        .password(userService.hashPassword("password")) //
        .firstName("Anonymous") //
        .lastName("User") //
        .email("anonymous@example.org") //
        .groups("anonymous");

    save(builder.build());

    builder = User.newBuilder() //
        .name("user1") //
        .password(userService.hashPassword("password")) //
        .firstName("Johnny B.") //
        .lastName("Good") //
        .email("user1@example.org") //
        .groups("group1", "group2");

    save(builder.build());

    builder = User.newBuilder() //
        .name("user2") //
        .password(userService.hashPassword("password")) //
        .firstName("Michel") //
        .lastName("Tremblay") //
        .email("user2@example.org") //
        .groups("group1", "group3");

    save(builder.build());
  }

  private void save(User user) {
    if(userService.findUser(user.getName()) == null) userService.save(user);
  }
}
