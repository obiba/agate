package org.obiba.agate.web.rest.user;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;

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
import org.obiba.agate.service.UserService;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsersJoinResourceTests {

  @InjectMocks
  UsersJoinResource resource;

  @Mock
  private UserService userService;

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

    doAnswer(invocation -> {
      Object[] args = invocation.getArguments();
      ((User) args[0]).setId("id");

      return null;
    }).when(userService).createUser(any(User.class));
  }

  @Test
  public void testUsersJoinWithAttributes() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameterMap()).thenReturn(new HashMap<String, String[]>() {
      {
        put("att1", new String[] { "1" });
        put("att2", new String[] { "foo" });
      }
    });

    ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);

    resource.create("un", "fn", "ln", "test@localhost.domain", "app", Lists.newArrayList("g1", "g2"), request);
    verify(userService).createUser(user.capture());
    assertEquals("id", user.getValue().getId());
    assertEquals("test@localhost.domain", user.getValue().getEmail());
    assertEquals("fn", user.getValue().getFirstName());
    assertEquals(Sets.newHashSet("g1", "g2"), user.getValue().getGroups());
    assertEquals(Sets.newHashSet("app"), user.getValue().getApplications());
  }

  @Test
  public void testUsersJoinMissingAttribute() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameterMap()).thenReturn(new HashMap<String, String[]>() {
      {
        put("att2", new String[] { "foo" });
      }
    });

    exception.expect(BadRequestException.class);
    exception.expectMessage(Matchers.containsString("att1"));
    resource.create("un", "fn", "ln", "test@localhost.domain", "app", Lists.newArrayList("g1", "g2"), request);
  }
}
