package org.obiba.agate.service;

import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.joda.time.LocalDate;
import org.obiba.agate.domain.User;
import org.obiba.agate.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  @Inject
  private UserRepository userRepository;

  @Inject
  private Environment env;

  public User findByUsername(@NotNull String username) {
    List<User> users = userRepository.findByName(username);
    return users == null || users.isEmpty() ? null : users.get(0);
  }

  public void updateCurrentUser(String firstName, String lastName, String email) {
    User currentUser = getCurrentUser();
    currentUser.setFirstName(firstName);
    currentUser.setLastName(lastName);
    currentUser.setEmail(email);
    userRepository.save(currentUser);
    log.debug("Changed information for User: {}", currentUser);
  }

  public void chnageCurrentUserPassword(String password) {
    User currentUser = getCurrentUser();
    currentUser.setPassword(hashPassword(password));
    userRepository.save(currentUser);
    log.debug("Changed password for User: {}", currentUser);
  }

  public void save(@NotNull User user) {
    userRepository.save(user);
  }

  public String hashPassword(String password) {
    RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "shiro.password.");
    return new Sha512Hash(password, propertyResolver.getProperty("salt"),
        propertyResolver.getProperty("nbHashIterations", Integer.class)).toString();
  }

  public User getCurrentUser() {
    String username = SecurityUtils.getSubject().getPrincipal().toString();
    User currentUser = findByUsername(username);
    if (currentUser == null) throw NoSuchUserException.withName(username);
    return currentUser;
  }

}
