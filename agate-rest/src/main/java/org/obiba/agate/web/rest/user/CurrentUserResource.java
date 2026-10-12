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

import com.google.common.collect.Maps;
import org.obiba.agate.domain.User;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.TotpService;
import org.obiba.agate.web.model.Agate;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Component
@Path("/user/_current")
public class CurrentUserResource extends AbstractUserResource {

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private TotpService totpService;

  @PUT
  public Response updateUser(Agate.UserDto userDto) {
    User current = getUser();
    if (!current.getId().equals(userDto.getId())) return Response.status(Response.Status.FORBIDDEN).build();
    // Self-service profile update: privileged attributes are kept from the persisted account and cannot be set by the caller.
    Agate.UserDto safeUserDto = Agate.UserDto.newBuilder(userDto)
      .setName(current.getName())
      .setRealm(current.getRealm())
      .setRole(current.getRole())
      .setStatus(current.getStatus().toString())
      .clearGroups().addAllGroups(current.getGroups())
      .clearApplications().addAllApplications(current.getApplications())
      .build();
    return super.updateUser(safeUserDto);
  }

  @PUT
  @Path("/_profile")
  @Consumes("application/x-www-form-urlencoded")
  public Response updateProfile(@Context HttpServletRequest servletRequest) {
    User current = getUser();
    Map<String, String[]> params = servletRequest.getParameterMap();
    Map<String, String> attributes = Maps.newHashMap();
    for (String key : params.keySet()) {
      if ("firstname".equals(key))
        current.setFirstName(params.get(key)[0]);
      else if ("lastname".equals(key))
        current.setLastName(params.get(key)[0]);
      else if ("locale".equals(key))
        current.setPreferredLanguage(params.get(key)[0]);
      else {
        configurationService.getConfiguration().getUserAttributes();
        attributes.put(key, params.get(key)[0]);
      }
    }
    current.setAttributes(attributes);
    userService.save(current);
    return Response.noContent().build();
  }

  @PUT
  @Path("/otp")
  @Produces("text/plain")
  public Response enableOtp() {
    User current = getUser();
    current.setSecret(totpService.generateSecret());
    userService.save(current);
    return Response.ok(totpService.getQrImageDataUri(current.getEmail(), current.getSecret()), "text/plain").build();
  }

  @DELETE
  @Path("/otp")
  public Response disableOtp() {
    User current = getUser();
    current.setSecret(null);
    userService.save(current);
    return Response.ok().build();
  }

  @Override
  protected User getUser() {
    return userService.getCurrentUser();
  }
}
