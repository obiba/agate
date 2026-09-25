/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NotificationTemplateServiceTest {

  @Test
  public void testFoldersOfAllLocationsSortedWithoutDuplicates() {
    NotificationTemplateService service = new NotificationTemplateService(new PathMatchingResourcePatternResolver(),
      "classpath:/notification-templates/first/, classpath:/notification-templates/second/, classpath:/notification-templates/missing/");

    // own.ftl, directly in notifications/, is an Agate email and not an application folder
    assertEquals(List.of("mica", "mica-a"), service.getFolders());
  }

  @Test
  public void testNoFolders() {
    NotificationTemplateService service = new NotificationTemplateService(new PathMatchingResourcePatternResolver(),
      "classpath:/notification-templates/missing/");

    assertTrue(service.getFolders().isEmpty());
  }
}
