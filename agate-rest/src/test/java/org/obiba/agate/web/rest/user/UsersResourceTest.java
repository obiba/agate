/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.user;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserStatus;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsersResourceTest {

  @InjectMocks
  private UsersPublicResource usersResource;

  @Mock
  private UserService userService;

  @Mock
  private ConfigurationService configurationService;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    User user = User.newBuilder().name("toto").build();
    user.setStatus(UserStatus.APPROVED);
    when(userService.findUser(anyString())).thenReturn(user);
    when(configurationService.decrypt(anyString())).thenReturn("toto");
  }

  @Test
  public void testConfirm() {
    usersResource.confirm("encryptedKey", "password");
    verify(userService).confirmUser(any(User.class), anyString());
  }
}
