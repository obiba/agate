package org.obiba.agate.service;

import jakarta.annotation.Nonnull;
import org.obiba.agate.domain.Group;
import org.obiba.agate.domain.User;
import org.obiba.agate.repository.GroupRepository;
import org.obiba.agate.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import jakarta.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GroupService {

  private final GroupRepository groupRepository;

  private final UserRepository userRepository;

  @Inject
  public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
  }

  /**
   * Find all {@link org.obiba.agate.domain.Group}.
   *
   * @return
   */
  public List<Group> findGroups() {
    return groupRepository.findAll();
  }

  /**
   * Find a {@link org.obiba.agate.domain.Group} by its name.
   *
   * @param name
   * @return null if not found
   */
  @Nullable
  public Group findGroup(@Nonnull String name) {
    Assert.notNull(name, "Group name cannot be null.");
    return groupRepository.findOneByName(name);
  }

  @Nullable
  public List<Group> findByApplication(@Nonnull String application) {
    Assert.notNull(application, "Application name cannot be null.");
    return groupRepository.findByApplications(application);
  }

  public void ensureGroupsByName(@Nonnull Collection<String> names) {
    Assert.notNull(names, "Group names cannot be null.");
    names.forEach(this::getGroup);
  }

  public List<User> getUsers(String name) {
    return userRepository.findByGroups(name);
  }

  /**
   * Get group with id and throws {@link org.obiba.agate.service.NoSuchGroupException} if not found.
   *
   * @param id
   * @return
   */
  public Group getGroup(String id) {
    Optional<Group> group = groupRepository.findById(id);
    if(!group.isPresent()) throw NoSuchGroupException.withId(id);
    return group.get();
  }

  /**
   * Insert of update a {@link org.obiba.agate.domain.Group}.
   *
   * @param group
   * @return
   */
  public Group save(@Nonnull Group group) {
    if(group.isNew()) {
      group.setNameAsId();
      groupRepository.insert(group);
    } else
      groupRepository.save(group);
    return group;
  }

  /**
   * Delete a {@link org.obiba.agate.domain.Group}.
   *
   * @param group
   */
  public void delete(@Nonnull Group group) {
    for(User user : userRepository.findAll()) {
      if(user.getGroups().contains(group.getName())) {
        throw NotOrphanGroupException.withName(group.getName());
      }
    }

    groupRepository.delete(group);
  }

}

