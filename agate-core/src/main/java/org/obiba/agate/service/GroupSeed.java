/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import javax.inject.Inject;

import org.obiba.agate.domain.Group;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class GroupSeed implements ApplicationListener<ContextRefreshedEvent> {

  @Inject
  private UserService userService;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    save(new Group("mica-administrator"));
    save(new Group("mica-reviewer"));
    save(new Group("mica-editor"));
    save(new Group("mica-user"));
  }

  private void save(Group group) {
    if(userService.findGroup(group.getName()) == null) userService.save(group);
  }
}
