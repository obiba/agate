/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.shiro.crypto.hash.Sha512Hash;
import org.obiba.agate.domain.Application;
import org.obiba.agate.domain.Group;
import org.obiba.agate.domain.User;
import org.obiba.agate.repository.ApplicationRepository;
import org.obiba.agate.repository.GroupRepository;
import org.obiba.agate.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    Application application = applicationRepository.findOne(id);

    if (application == null) throw NoSuchApplicationException.withId(id);

    return application;
  }

  public Application find(@NotNull String id) {
    return applicationRepository.findOne(id);
  }

  public List<Application> findAll() {
    return applicationRepository.findAll();
  }

  public Application findByName(@NotNull String name) {
    List<Application> applications = applicationRepository.findByName(name);
    return applications == null || applications.isEmpty() ? null : applications.get(0);
  }

  public boolean isValid(String name, String key) {
    List<Application> applications = applicationRepository.findByNameAndKey(name, hashKey(key));
    return applications != null && !applications.isEmpty();
  }

  public void save(@NotNull Application application) {
    if (application.isNew()) application.setNameAsId();
    applicationRepository.save(application);
  }

  public void delete(@NotNull String id) {
    Application application = getApplication(id);
    List<User> users = userRepository.findByApplications(application.getName());
    List<Group> groups = groupRepository.findByApplications(application.getName());

    if(!users.isEmpty() || !groups.isEmpty()) {
      throw NotOrphanApplicationException.withName(application.getName());
    }

    applicationRepository.delete(application);
  }

  public String hashKey(String key) {
    RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "shiro.password.");
    return new Sha512Hash(key, propertyResolver.getProperty("salt"),
      propertyResolver.getProperty("nbHashIterations", Integer.class)).toString();
  }
}
