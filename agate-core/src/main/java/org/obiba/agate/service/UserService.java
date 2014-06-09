package org.obiba.agate.service;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.obiba.agate.domain.Group;
import org.obiba.agate.domain.User;
import org.obiba.agate.repository.GroupRepository;
import org.obiba.agate.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.env.Environment;
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
  private GroupRepository groupRepository;

  @Inject
  private Environment env;

  /**
   * Find all {@link org.obiba.agate.domain.User}.
   * @return
   */
  public List<User> findUsers() {
    return userRepository.findAll();
  }

  /**
   * Find a {@link org.obiba.agate.domain.User} by its name.
   * @param username
   * @return null if not found
   */
  public @Nullable User findUser(@NotNull String username) {
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

  public void changeCurrentUserPassword(String password) {
    User currentUser = getCurrentUser();
    currentUser.setPassword(hashPassword(password));
    userRepository.save(currentUser);
    log.debug("Changed password for User: {}", currentUser);
  }

  /**
   * Insert or update a {@link org.obiba.agate.domain.User}.
   * @param user
   * @return
   */
  public User save(@NotNull User user) {
    userRepository.save(user);
    if (user.getGroups() != null) {
      for(String groupName : user.getGroups()) {
        Group group = findGroup(groupName);
        if(group == null) groupRepository.save(new Group(groupName));
      }
    }
    return user;
  }

  /**
   * Delete a {@link org.obiba.agate.domain.User}.
   * @param id
   */
  public void delete(@NotNull String id) {
    userRepository.delete(id);
  }

  /**
   * Delete a {@link org.obiba.agate.domain.User}.
   * @param user
   */
  public void delete(@NotNull User user) {
    userRepository.delete(user);
  }

  /**
   * Get user with id and throws {@link org.obiba.agate.service.NoSuchUserException} if not found.
   * @param id
   * @return
   */
  public User getUser(String id) {
    User user = userRepository.findOne(id);
    if (user == null) throw NoSuchUserException.withId(id);
    return user;
  }

  /**
   * Get currently logged user and throws {@link org.obiba.agate.service.NoSuchUserException} if not found.
   * @param id
   * @return
   */
  public User getCurrentUser() {
    String username = SecurityUtils.getSubject().getPrincipal().toString();
    User currentUser = findUser(username);
    if (currentUser == null) throw NoSuchUserException.withName(username);
    return currentUser;
  }

  /**
   * Hash user password.
   * @param password
   * @return
   */
  public String hashPassword(String password) {
    RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "shiro.password.");
    return new Sha512Hash(password, propertyResolver.getProperty("salt"),
        propertyResolver.getProperty("nbHashIterations", Integer.class)).toString();
  }

  //
  // Group methods
  //

  /**
   * Find all {@link org.obiba.agate.domain.Group}.
   * @return
   */
  public List<Group> findGroups() {
    return groupRepository.findAll();
  }

  /**
   * Find a {@link org.obiba.agate.domain.Group} by its name.
   * @param name
   * @return null if not found
   */
  public @Nullable Group findGroup(@NotNull String name) {
    List<Group> groups = groupRepository.findByName(name);
    return groups == null || groups.isEmpty() ? null : groups.get(0);
  }

  /**
   * Get group with id and throws {@link org.obiba.agate.service.NoSuchGroupException} if not found.
   * @param id
   * @return
   */
  public Group getGroup(String id) {
    Group group = groupRepository.findOne(id);
    if (group == null) throw NoSuchGroupException.withId(id);
    return group;
  }

  /**
   * Insert of update a {@link org.obiba.agate.domain.Group}.
   * @param group
   * @return
   */
  public Group save(@NotNull Group group) {
    groupRepository.save(group);
    return group;
  }

  /**
   * Delete a {@link org.obiba.agate.domain.Group}.
   * @param user
   */
  public void delete(@NotNull Group group) {
    for (User user : userRepository.findAll()) {
      if (user.getGroups().contains(group.getName())) {
        throw NotOrphanGroupException.withName(group.getName());
      }
    }
    groupRepository.delete(group);
  }

}
