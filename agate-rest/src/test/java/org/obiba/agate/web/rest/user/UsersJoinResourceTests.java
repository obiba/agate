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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.obiba.agate.domain.AttributeConfiguration;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.ReCaptchaService;
import org.obiba.agate.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class UsersJoinResourceTests {

  @InjectMocks
  UsersPublicResource resource;

  @Mock
  private UserService userService;

  @Mock
  private ReCaptchaService reCaptchaService;

  @Mock
  private ConfigurationService configurationService;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);

    Configuration conf = new Configuration();
    conf.setUserAttributes(Lists.newArrayList(
        new AttributeConfiguration("att1", AttributeConfiguration.Type.INTEGER, true, Lists.newArrayList())));

    doReturn(conf).when(configurationService).getConfiguration();

    when(reCaptchaService.verify(anyString())).thenReturn(true);

    doAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      ((User) args[0]).setId("id");

      return null;
    }).when(userService).createUser(any(User.class), any(String.class));
  }

  @Test
  public void testUsersJoinWithAttributes() {
    HttpServletRequest request = mock(HttpServletRequest.class);

    ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);

    MultivaluedMap<String, String> params = getParameters();
    params.put("att1", Lists.newArrayList("1"));
    params.put("att2", Lists.newArrayList("foo"));

    resource.create(request, params);
    verify(userService).createUser(user.capture(), eq("password"));
    assertEquals("id", user.getValue().getId());
    assertEquals("test@localhost.domain", user.getValue().getEmail());
    assertEquals("fr", user.getValue().getPreferredLanguage());
    assertEquals("fn", user.getValue().getFirstName());
    assertEquals(Sets.newHashSet("g1", "g2"), user.getValue().getGroups());
    assertEquals(Sets.newHashSet("app"), user.getValue().getApplications());
  }

  @Test
  public void testUsersJoinMissingAttribute() {
    HttpServletRequest request = mock(HttpServletRequest.class);

    exception.expect(BadRequestException.class);
    exception.expectMessage(Matchers.containsString("att1"));

    MultivaluedMap<String, String> params = getParameters();
    params.put("att2", Lists.newArrayList("foo"));
    resource.create(request, params);
  }

  private MultivaluedMap<String, String> getParameters() {
    MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
    params.put("username", Lists.newArrayList("un"));
    params.put("firstname", Lists.newArrayList("fn"));
    params.put("lastname", Lists.newArrayList("ln"));
    params.put("email", Lists.newArrayList("test@localhost.domain"));
    params.put("locale", Lists.newArrayList("fr"));
    params.put("application", Lists.newArrayList("app"));
    params.put("group", Lists.newArrayList("g1", "g2"));
    params.put("password", Lists.newArrayList("password"));
    params.put("g-recaptcha-response", Lists.newArrayList("recaptchacode"));
    return params;
  }
}
