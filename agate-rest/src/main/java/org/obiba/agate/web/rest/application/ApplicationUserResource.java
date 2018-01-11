/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.application;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.obiba.agate.domain.Application;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.ApplicationService;
import org.obiba.agate.service.NoSuchUserException;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.model.Dtos;
import org.obiba.shiro.realm.ObibaRealm;
import org.obiba.web.model.AuthDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
@Path("/application/{id}/user/{username}")
public class ApplicationUserResource extends ApplicationAwareResource {
  private static final Logger log = LoggerFactory.getLogger(ApplicationUserResource.class);

  @Inject
  private Dtos dtos;

  @Inject
  private ApplicationService applicationService;

  @Inject
  protected UserService userService;

  @GET
  public AuthDtos.SubjectDto get(@PathParam("id") String id, @PathParam("username") String username,
    @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader, @QueryParam("group") String group) {

    validateApplication(authHeader);
    Application application = applicationService.getApplication(id);

    List<User> users = Strings.isNullOrEmpty(group)
      ? userService.findActiveUserByApplication(username, application.getName())
      : userService.findActiveUserByApplicationAndGroup(username, application.getName(), group);

    if (users.isEmpty()) NoSuchUserException.withName(username);
    return dtos.asDto(users.get(0), true);
  }
}
