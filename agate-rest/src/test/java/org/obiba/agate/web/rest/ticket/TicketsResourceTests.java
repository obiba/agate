/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.ticket;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.Group;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserStatus;
import org.obiba.agate.service.*;
import org.obiba.agate.web.rest.security.AuthorizationValidator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class TicketsResourceTests {
  @InjectMocks
  private TicketsResource ticketsResource;

  @Mock
  private GroupService groupService;

  @Mock
  private UserService userService;

  @Mock
  private AuthorizationValidator authorizationValidator;

  @Mock
  private TicketService ticketService;

  @Mock
  private TokenUtils tokenUtils;

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ConfigurationService configurationService;

  @Mock
  private HttpServletRequest httpServletRequest;

  @Mock
  private org.apache.shiro.mgt.SecurityManager securityManager;

  @Mock
  private Subject subject;

  @Mock
  private PrincipalCollection principalColl;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    when(ticketService.findByUsername(anyString())).thenReturn(Lists.newArrayList());
    when(tokenUtils.makeAccessToken(anyObject())).thenReturn("token123");
    when(applicationService.isValid(anyString(), anyString())).thenReturn(true);
    Configuration configuration = new Configuration();
    configuration.setLongTimeout(30000);
    configuration.setShortTimeout(30000);
    configuration.setDomain("localhost");
    when(configurationService.getConfiguration()).thenReturn(configuration);
    when(authorizationValidator.validateApplication(anyString())).thenReturn("mica");
  }

  @Test
  public void testLoginWhenGroupHasAssociatedApplication() {
    when(principalColl.getRealmNames()).thenReturn(Sets.newHashSet("realm1"));
    when(subject.getPrincipals()).thenReturn(principalColl);
    when(securityManager.createSubject(any())).thenReturn(subject);
    SecurityUtils.setSecurityManager(securityManager);

    Ticket ticket = new Ticket();
    doReturn(ticket).when(ticketService).create("toto", false, false, "mica");

    User user = new User("toto", "realm1");
    user.setStatus(UserStatus.ACTIVE);
    user.setGroups(Sets.newHashSet("group1"));
    Group group = new Group("group1");
    group.setApplications(Sets.newHashSet("mica"));

    when(userService.findActiveUser(anyString())).thenReturn(user);
    when(groupService.findGroup("group1")).thenReturn(group);

    Response res = ticketsResource.login(httpServletRequest, false, false, "toto", "password", "Basic bWljYTpwYXNzd29yZA=="); //Basic mica:password

    assertEquals(201, res.getStatus());
  }
}
