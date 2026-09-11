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

import jakarta.annotation.Nonnull;
import org.joda.time.DateTime;
import org.obiba.agate.domain.Application;
import org.obiba.agate.domain.Group;
import org.obiba.agate.domain.User;
import org.obiba.agate.repository.ApplicationRepository;
import org.obiba.agate.repository.GroupRepository;
import org.obiba.agate.repository.UserRepository;
import org.obiba.agate.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.inject.Inject;
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
  private PasswordHasher passwordHasher;

  public Application getApplication(@Nonnull String id) throws NoSuchApplicationException {
    Optional<Application> application = applicationRepository.findById(id);

    if(!application.isPresent()) throw NoSuchApplicationException.withId(id);

    return application.get();
  }

  public Application find(@Nonnull String id) {
    return applicationRepository.findById(id).orElse(null);
  }

  public List<Application> findAll() {
    return applicationRepository.findAll();
  }

  public Application findByName(@Nonnull String name) {
    List<Application> applications = applicationRepository.findByName(name);
    return applications == null || applications.isEmpty() ? null : applications.get(0);
  }

  public Application findByIdOrName(@Nonnull String idOrName) {
    Application application = find(Application.idFromName(idOrName));
    if (application == null) {
      List<Application> applications = applicationRepository.findByName(idOrName);
      return applications == null || applications.isEmpty() ? null : applications.get(0);
    }
    return application;
  }


  /**
   * Check the application credentials. A key hashed by an earlier version is replaced with a current hash on
   * successful check.
   *
   * @param idOrName
   * @param key
   * @return
   */
  public boolean isValid(String idOrName, String key) {
    Application application = find(idOrName);
    if (application == null) application = findByName(idOrName);
    if (application == null || !passwordHasher.matches(key, application.getKey())) return false;

    if (passwordHasher.isLegacy(application.getKey())) {
      application.setKey(passwordHasher.hash(key));
      save(application);
    }
    return true;
  }

  public void save(@Nonnull Application application) {
    if(application.isNew()) {
      generateId(application);
      applicationRepository.insert(application);
    } else {
      application.setLastModifiedDate(new DateTime());
      applicationRepository.save(application);
    }
  }

  public void delete(@Nonnull String id) {
    Application application = getApplication(id);
    List<User> users = userRepository.findByApplications(application.getId());
    List<Group> groups = groupRepository.findByApplications(application.getId());

    if(!users.isEmpty() || !groups.isEmpty()) {
      throw NotOrphanApplicationException.withId(application.getId());
    }

    applicationRepository.delete(application);
  }

  public String hashKey(String key) {
    return passwordHasher.hash(key);
  }

  //
  // Private methods
  //

  private void generateId(@Nonnull Application application) {
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
