/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
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
import javax.ws.rs.BadRequestException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.obiba.agate.domain.Application;
import org.obiba.agate.domain.Group;
import org.obiba.agate.service.ApplicationService;
import org.obiba.agate.service.UserService;
import org.obiba.agate.web.model.Agate;
import org.obiba.agate.web.model.Dtos;
import org.obiba.agate.web.rest.config.JerseyConfiguration;
import org.obiba.agate.web.rest.user.GroupResource;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

@Component
@RequiresRoles("AGATE_ADMIN")
@Path("/applications")
public class ApplicationsResource {

  @Inject
  private ApplicationService applicationService;

  @Inject
  private Dtos dtos;

  @GET
  public List<Agate.ApplicationDto> get() {
    ImmutableList.Builder<Agate.ApplicationDto> builder = ImmutableList.builder();
    for(Application application : applicationService.findAll()) {
      builder.add(dtos.asDto(application));
    }
    return builder.build();
  }

}
