/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import org.apache.shiro.crypto.hash.Sha512Hash;
import org.obiba.agate.domain.Application;
import org.obiba.agate.domain.Group;
import org.obiba.agate.domain.User;
import org.obiba.agate.repository.ApplicationRepository;
import org.obiba.agate.repository.GroupRepository;
import org.obiba.agate.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing applications.
 */
@Service
@Transactional
public class ApplicationService {

  private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

  @Inject
  private ApplicationRepository applicationRepository;

  @Inject
  private UserRepository userRepository;

  @Inject
  private GroupRepository groupRepository;

  @Inject
  private Environment env;

  public Application getApplication(@NotNull String id) throws NoSuchApplicationException {
    Optional<Application> application = applicationRepository.findById(id);

    if(!application.isPresent()) throw NoSuchApplicationException.withId(id);

    return application.get();
  }

  public Application find(@NotNull String id) {
    return applicationRepository.findById(id).orElse(null);
  }

  public List<Application> findAll() {
    return applicationRepository.findAll();
  }

  public Application findByName(@NotNull String name) {
    List<Application> applications = applicationRepository.findByName(name);
    return applications == null || applications.isEmpty() ? null : applications.get(0);
  }

  public Application findByIdOrName(@NotNull String idOrName) {
    Application application = find(idOrName);
    if (application == null) {
      List<Application> applications = applicationRepository.findByName(idOrName);
      return applications == null || applications.isEmpty() ? null : applications.get(0);
    }
    return application;
  }


  public boolean isValid(String idOrName, String key) {
    List<Application> applications = applicationRepository.findByIdAndKey(idOrName, hashKey(key));
    if(applications == null || applications.isEmpty())
      applications = applicationRepository.findByNameAndKey(idOrName, hashKey(key));
    return applications != null && !applications.isEmpty();
  }

  public void save(@NotNull Application application) {
    if(application.isNew()) {
      generateId(application);
      applicationRepository.insert(application);
    } else
      applicationRepository.save(application);
  }

  public void delete(@NotNull String id) {
    Application application = getApplication(id);
    List<User> users = userRepository.findByApplications(application.getId());
    List<Group> groups = groupRepository.findByApplications(application.getId());

    if(!users.isEmpty() || !groups.isEmpty()) {
      throw NotOrphanApplicationException.withId(application.getId());
    }

    applicationRepository.delete(application);
  }

  public String hashKey(String key) {
    return new Sha512Hash(key, env.getProperty("shiro.password.salt"), env.getProperty("shiro.password.nbHashIterations", Integer.class, 10000)).toString();
  }

  //
  // Private methods
  //

  private void generateId(@NotNull Application application) {
    application.setNameAsId();
    String id = application.getId();
    Application found = find(id);
    int i = 1;
    while(found != null) {
      application.setId(id + i);
      i++;
      found = find(application.getId());
    }
  }
}
