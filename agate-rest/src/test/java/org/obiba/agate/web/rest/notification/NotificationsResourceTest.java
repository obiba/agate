/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.notification;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.MailService;
import org.obiba.agate.service.ReCaptchaService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.rest.security.AuthorizationValidator;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NotificationsResourceTest {

  private static final String APP_AUTH = "app-auth-header";

  @InjectMocks
  private NotificationsResource resource;

  @Mock
  private ReCaptchaService reCaptchaService;

  @Mock
  private MailService mailService;

  @Mock
  private UserService userService;

  @Mock
  private AuthorizationValidator authorizationValidator;

  @BeforeEach
  public void init() {
    when(authorizationValidator.validateApplication(APP_AUTH)).thenReturn("mica");
    when(userService.findActiveUsersByApplication("mica"))
      .thenReturn(Lists.newArrayList(User.newBuilder("user1").build()));
  }

  @Test
  public void testTemplateEmailRejectedOnInvalidReCaptcha() {
    when(reCaptchaService.verify("junk")).thenReturn(false);

    assertThrows(BadRequestException.class, () -> resource.notify(formParams("contactUs", "junk"), APP_AUTH));

    verify(reCaptchaService).verify("junk");
    verifyNoInteractions(mailService);
  }

  @Test
  public void testPlainEmailRejectedOnInvalidReCaptcha() {
    when(reCaptchaService.verify("junk")).thenReturn(false);

    assertThrows(BadRequestException.class, () -> resource.notify(formParams(null, "junk"), APP_AUTH));

    verifyNoInteractions(mailService);
  }

  @Test
  public void testPlainEmailSentOnValidReCaptcha() {
    when(reCaptchaService.verify("valid-token")).thenReturn(true);

    assertEquals(204, resource.notify(formParams(null, "valid-token"), APP_AUTH).getStatus());

    verify(reCaptchaService).verify("valid-token");
    verify(mailService).sendEmail(anyString(), eq("subject"), eq("body"));
  }

  @Test
  public void testNoReCaptchaIsNotVerified() {
    assertEquals(204, resource.notify(formParams(null, null), APP_AUTH).getStatus());

    // backend triggered notifications carry no reCaptcha response and must not be verified
    verifyNoInteractions(reCaptchaService);
    verify(mailService).sendEmail(anyString(), eq("subject"), eq("body"));
  }

  private MultivaluedMap<String, String> formParams(String template, String reCaptcha) {
    MultivaluedMap<String, String> formParams = new MultivaluedHashMap<>();
    formParams.putSingle("subject", "subject");
    if (template == null) formParams.putSingle("body", "body");
    else formParams.putSingle("template", template);
    if (reCaptcha != null) formParams.putSingle("reCaptcha", reCaptcha);
    return formParams;
  }
}
