/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.obiba.agate.domain.Application;
import org.obiba.agate.service.ApplicationService;
import org.obiba.agate.web.model.Agate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicationResourceTest {

  @InjectMocks
  private ApplicationResource resource;

  @Mock
  private ApplicationService applicationService;

  @Test
  public void testUpdateSetsNotificationsTemplate() {
    Application application = new Application("mica-a");
    when(applicationService.getApplication("mica-a")).thenReturn(application);

    resource.updateApplication("mica-a",
      Agate.ApplicationDto.newBuilder().setName("mica-a").setNotificationsTemplate("mica").build());

    assertEquals("mica", application.getNotificationsTemplate());
    verify(applicationService).save(application);
  }

  @Test
  public void testUpdateClearsNotificationsTemplate() {
    Application application = new Application("mica-a");
    application.setNotificationsTemplate("mica");
    when(applicationService.getApplication("mica-a")).thenReturn(application);

    resource.updateApplication("mica-a",
      Agate.ApplicationDto.newBuilder().setName("mica-a").setNotificationsTemplate("").build());

    assertFalse(application.hasNotificationsTemplate());
    verify(applicationService).save(application);
  }

  @Test
  public void testUpdateKeepsNotificationsTemplateWhenNotProvided() {
    Application application = new Application("mica-a");
    application.setNotificationsTemplate("mica");
    when(applicationService.getApplication("mica-a")).thenReturn(application);

    resource.updateApplication("mica-a", Agate.ApplicationDto.newBuilder().setName("mica-a").build());

    assertEquals("mica", application.getNotificationsTemplate());
    verify(applicationService).save(application);
  }
}
