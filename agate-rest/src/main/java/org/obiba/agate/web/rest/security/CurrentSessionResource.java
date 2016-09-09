/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.agate.web.rest.security;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.subject.Subject;
import org.obiba.agate.security.Roles;
import org.obiba.agate.web.model.Agate;
import org.springframework.stereotype.Component;

@Component
@Path("/auth/session/_current")
@RequiresRoles("agate-user")
public class CurrentSessionResource {

  @DELETE
  public Response deleteSession() {
    // Delete the Shiro session
    try {
      SecurityUtils.getSubject().logout();
    } catch(InvalidSessionException e) {
      // Ignore
    }
    return Response.ok().build();
  }

  @GET
  public Agate.SessionDto get() {
    Subject subject = SecurityUtils.getSubject();
    Agate.SessionDto.Builder builder = Agate.SessionDto.newBuilder() //
        .setUsername(subject.getPrincipal().toString()) //
        .setRealm(subject.getPrincipals().getRealmNames().iterator().next());

    try {
      subject.checkRole(Roles.AGATE_ADMIN.toString());
      builder.setRole(Roles.AGATE_ADMIN.toString());
    } catch(AuthorizationException e) {
      builder.setRole(Roles.AGATE_USER.toString());
    }

    return builder.build();
  }

  @GET
  @Path("/username")
  public Response getSubject() {
    // Find the Shiro username
    return Response.ok(SecurityUtils.getSubject().getPrincipal().toString()).build();
  }

}

