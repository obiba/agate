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

import org.obiba.agate.domain.User;
import org.obiba.agate.security.Roles;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

@Component
public class UserSeed implements ApplicationListener<ContextRefreshedEvent> {

  @Inject
  private UserService userService;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    User user = new User();
    user.setName("test1");
    user.setPassword(userService.hashPassword("pwel1"));
    user.setFirstName("Johnny B.");
    user.setLastName("Good");
    user.setEmail("test1@patate.com");
    user.setGroups(Sets.newHashSet("group1", "group2"));

    userService.save(user);

    user = new User();
    user.setName("test2");
    user.setPassword(userService.hashPassword("pwel2"));
    user.setFirstName("Michel");
    user.setLastName("Tremblay");
    user.setEmail("test2@patate.com");
    user.setRole(Roles.AGATE_ADMIN);
    user.setGroups(Sets.newHashSet("group1", "group3", "mica-administrator"));

    userService.save(user);
  }
}
