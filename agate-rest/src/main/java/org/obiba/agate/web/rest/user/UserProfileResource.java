/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.rest.user;

import org.obiba.agate.domain.User;
import org.obiba.agate.service.NoSuchUserException;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.model.Dtos;
import org.obiba.agate.web.rest.application.ApplicationAwareResource;
import org.obiba.shiro.realm.ObibaRealm;
import org.obiba.web.model.AuthDtos;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;

@Component
@Path("/user/{username}/profile")
public class UserProfileResource extends ApplicationAwareResource {

  @Inject
  private Dtos dtos;

  @Inject
  private UserService userService;

  @GET
  public AuthDtos.SubjectDto getProfile(
    @PathParam("username") String username,
    @QueryParam("locale") @DefaultValue("en") String locale,
    @HeaderParam(ObibaRealm.APPLICATION_AUTH_HEADER) String authHeader) {

    validateApplication(authHeader);
    User user = userService.findActiveUser(username);
    if (user == null) throw NoSuchUserException.withName(username);
    return dtos.asDto(user, true, locale);
  }
}
