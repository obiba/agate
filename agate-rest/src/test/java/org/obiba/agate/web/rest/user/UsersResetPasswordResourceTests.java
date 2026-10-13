/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserCredentials;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.UserService;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UsersResetPasswordResourceTests {

  private static final String KEY = "encrypted-key";

  private static final String PASSWORD_HASH = "stored-password-hash";

  @InjectMocks
  UsersPublicResource resource;

  @Mock
  private UserService userService;

  @Mock
  private ConfigurationService configurationService;

  private final User user = User.newBuilder().name("alice").active().build();

  @BeforeEach
  public void init() {
    MockitoAnnotations.initMocks(this);
    when(userService.findUser("alice")).thenReturn(user);
    when(userService.findUserCredentials("alice")).thenReturn(new UserCredentials("alice", PASSWORD_HASH));
  }

  @Test
  public void testResetPasswordWithValidKey() throws Exception {
    mockKey(UserService.getPasswordDigest(new UserCredentials("alice", PASSWORD_HASH)));

    Response response = resource.resetPassword(KEY, "NewPass1!");

    assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    verify(userService).updateUserPassword(user, "NewPass1!");
  }

  @Test
  public void testResetPasswordRejectsKeyIssuedForPreviousPassword() throws Exception {
    // password changed since the key was issued (e.g. the key was already redeemed)
    mockKey(UserService.getPasswordDigest(new UserCredentials("alice", "previous-password-hash")));

    assertThrows(BadRequestException.class, () -> resource.resetPassword(KEY, "NewPass1!"));
    verify(userService, never()).updateUserPassword(any(User.class), anyString());
  }

  @Test
  public void testResetPasswordRejectsRawPasswordHash() throws Exception {
    // the key carries a digest of the hash, never the hash itself
    mockKey(PASSWORD_HASH);

    assertThrows(BadRequestException.class, () -> resource.resetPassword(KEY, "NewPass1!"));
    verify(userService, never()).updateUserPassword(any(User.class), anyString());
  }

  @Test
  public void testResetPasswordRejectsKeyWithoutHash() throws Exception {
    // key issued before keys were bound to the credentials
    mockKey(null);

    assertThrows(BadRequestException.class, () -> resource.resetPassword(KEY, "NewPass1!"));
    verify(userService, never()).updateUserPassword(any(User.class), anyString());
  }

  @Test
  public void testResetPasswordRejectsExpiredKey() throws Exception {
    mockKey(UserService.getPasswordDigest(new UserCredentials("alice", PASSWORD_HASH)), DateTime.now().minusMinutes(1));

    assertThrows(BadRequestException.class, () -> resource.resetPassword(KEY, "NewPass1!"));
    verify(userService, never()).updateUserPassword(any(User.class), anyString());
  }

  private void mockKey(String hash) throws Exception {
    mockKey(hash, DateTime.now().plusHours(1));
  }

  private void mockKey(String hash, DateTime expire) throws Exception {
    Map<String, String> data = Maps.newHashMap();
    data.put("username", "alice");
    data.put("expire", expire.toString());
    if (hash != null) data.put("hash", hash);
    when(configurationService.decrypt(KEY)).thenReturn(new ObjectMapper().writeValueAsString(data));
  }
}
