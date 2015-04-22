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
import org.obiba.agate.repository.UserCredentialsRepository;
import org.obiba.agate.repository.UserRepository;

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
  public void testUserConfirmation() {
    User user = User.newBuilder().name("toto").active().build();
    userService.confirmUser(user, "p4ssw0rd");
    assertTrue(user.getStatus() == UserStatus.ACTIVE);
  }

  @Test
  public void testSavePublishesJoinEvent() {
    User user = User.newBuilder().name("toto").pending().build();
    userService.save(user);
    verify(eventBus).post(any(UserJoinedEvent.class));
  }

  @Test
  public void testSavePublishesApprovedEvent() {
    User user = User.newBuilder().name("toto").build();
    user.setStatus(UserStatus.APPROVED);
    userService.save(user);
    verify(eventBus).post(any(UserApprovedEvent.class));
  }
}
