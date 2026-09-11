/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.agate.security;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.Enforced2FAStrategy;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserCredentials;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.TotpService;
import org.obiba.agate.service.UserService;
import org.obiba.shiro.NoSuchOtpException;
import org.obiba.shiro.authc.UsernamePasswordOtpToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * The password must be verified before anything related to the one-time password (issue #560).
 */
class AgateUserRealmTest {

  private static final String USERNAME = "jdoe";
  private static final String PASSWORD = "secret";
  private static final String HASH = "hash";

  @Mock
  private UserService userService;

  @Mock
  private ConfigurationService configurationService;

  @Mock
  private TotpService totpService;

  @Mock
  private PasswordHasher passwordHasher;

  @InjectMocks
  private AgateUserRealm realm;

  private Configuration configuration;

  private User user;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    realm.afterPropertiesSet();

    configuration = new Configuration();
    configuration.setOtpStrategy("TOTP");
    when(configurationService.getConfiguration()).thenReturn(configuration);

    user = User.newBuilder(USERNAME).realm(AgateRealm.AGATE_USER_REALM.getName()).active().build();
    when(userService.findActiveUser(USERNAME)).thenReturn(user);
    when(userService.findUserCredentials(USERNAME)).thenReturn(new UserCredentials(USERNAME, HASH));

    when(passwordHasher.matches(PASSWORD, HASH)).thenReturn(true);
  }

  @Test
  void wrongPasswordFailsBeforeOtpChallengeWhen2FAEnforced() {
    configuration.setEnforced2FAStrategy(Enforced2FAStrategy.APP);

    assertThrows(IncorrectCredentialsException.class,
        () -> realm.getAuthenticationInfo(new UsernamePasswordToken(USERNAME, "wrong")));

    verifyNoInteractions(totpService);
    verify(userService, never()).save(any(User.class));
  }

  @Test
  void wrongPasswordFailsBeforeOtpChallengeWhenUserHasSecret() {
    user.setSecret("abc");

    assertThrows(IncorrectCredentialsException.class,
        () -> realm.getAuthenticationInfo(new UsernamePasswordOtpToken(USERNAME, "wrong", "123456")));

    verifyNoInteractions(totpService);
  }

  @Test
  void wrongPasswordDoesNotConfirmTemporarySecret() {
    configuration.setEnforced2FAStrategy(Enforced2FAStrategy.APP);
    user.resetSecret("tmp");
    when(totpService.validateCode("123456", "tmp")).thenReturn(true);

    assertThrows(IncorrectCredentialsException.class,
        () -> realm.getAuthenticationInfo(new UsernamePasswordOtpToken(USERNAME, "wrong", "123456")));

    assertFalse(user.hasSecret());
    assertTrue(user.hasTempSecret());
    verify(userService, never()).save(any(User.class));
  }

  @Test
  void rightPasswordWithoutCodeRequestsOtpWhen2FAEnforced() {
    configuration.setEnforced2FAStrategy(Enforced2FAStrategy.APP);

    assertThrows(NoSuchOtpException.class,
        () -> realm.getAuthenticationInfo(new UsernamePasswordToken(USERNAME, PASSWORD)));
  }

  @Test
  void rightPasswordWithWrongCodeFails() {
    user.setSecret("abc");
    when(totpService.validateCode("000000", "abc")).thenReturn(false);

    AuthenticationException e = assertThrows(AuthenticationException.class,
        () -> realm.getAuthenticationInfo(new UsernamePasswordOtpToken(USERNAME, PASSWORD, "000000")));
    assertFalse(e instanceof IncorrectCredentialsException);
    assertEquals("Wrong TOTP", e.getMessage());
  }

  @Test
  void rightPasswordWithValidCodeConfirmsTemporarySecret() {
    configuration.setEnforced2FAStrategy(Enforced2FAStrategy.APP);
    user.resetSecret("tmp");
    when(totpService.validateCode("123456", "tmp")).thenReturn(true);

    assertNotNull(realm.getAuthenticationInfo(new UsernamePasswordOtpToken(USERNAME, PASSWORD, "123456")));

    assertEquals("tmp", user.getSecret());
    assertFalse(user.hasTempSecret());
    verify(userService).save(user);
  }

  @Test
  void no2FAAuthenticatesWithPasswordOnly() {
    assertNotNull(realm.getAuthenticationInfo(new UsernamePasswordToken(USERNAME, PASSWORD)));
    verifyNoInteractions(totpService);
  }
}
