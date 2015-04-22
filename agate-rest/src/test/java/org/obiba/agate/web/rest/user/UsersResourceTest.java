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
import org.obiba.agate.web.model.Agate;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsersResourceTest {

  @InjectMocks
  private UsersResource usersResource;

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
    when(configurationService.encrypt(anyString())).thenReturn("encryptedKey");
  }

  @Test
  public void testConfirm() {
    usersResource.confirm(Agate.ConfirmForm.newBuilder().setUsername("toto").setKey("encryptedKey").setPassword("password").build());
    verify(userService).confirmUser(any(User.class), anyString());
  }
}
