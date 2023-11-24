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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserStatus;
import org.obiba.agate.event.UserApprovedEvent;
import org.obiba.agate.event.UserJoinedEvent;
import org.obiba.agate.repository.RealmConfigRepository;
import org.obiba.agate.repository.UserCredentialsRepository;
import org.obiba.agate.repository.UserRepository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.core.env.Environment;

import com.google.common.eventbus.EventBus;

public class UserServiceTest {

  @InjectMocks
  private UserService userService;

  @Mock
  private UserCredentialsRepository userCredentialsRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private Environment environment;

  @Mock
  private EventBus eventBus;

  @Mock
  private ConfigurationService configurationService;

  @Mock
  private RealmConfigRepository realmConfigRepository;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    when(environment.containsProperty(anyString())).thenReturn(true);
    when(environment.getProperty(matches(".*salt"), any(Class.class))).thenReturn("salt");
    when(environment.getProperty(matches(".*terations"), any(Class.class))).thenReturn(1);

    Configuration conf = new Configuration();

    doReturn(conf).when(configurationService).getConfiguration();
  }

  @Test
  public void testPasswordPattern() {
    assertFalse(UserService.PWD_PATTERN.matcher("").matches());
    assertFalse(UserService.PWD_PATTERN.matcher("password").matches());
    assertFalse(UserService.PWD_PATTERN.matcher("PASSWORD").matches());
    assertFalse(UserService.PWD_PATTERN.matcher("12345678").matches());
    assertFalse(UserService.PWD_PATTERN.matcher("@#$%^&+=!").matches());
    assertFalse(UserService.PWD_PATTERN.matcher("P@0rd").matches());
    assertTrue(UserService.PWD_PATTERN.matcher("P@ssw0rd").matches());
    assertTrue(UserService.PWD_PATTERN.matcher("Pa$$w0rd").matches());
  }

  @Test
  public void testUserConfirmation() {
    User user = User.newBuilder().name("toto").active().build();
    userService.confirmUser(user, "P@ssw0rd");
    assertTrue(user.getStatus() == UserStatus.ACTIVE);
  }

  @Test
  public void testSavePublishesJoinEvent() {
    User user = User.newBuilder().name("toto").pending().build();
    userService.createUser(user, null);
    verify(eventBus).post(any(UserJoinedEvent.class));
  }

  @Test
  public void testSavePublishesApprovedEvent() {
    User user = User.newBuilder().name("toto").build();
    user.setStatus(UserStatus.APPROVED);
    userService.createUser(user, null);
    verify(eventBus).post(any(UserApprovedEvent.class));
  }
}
