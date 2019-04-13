package org.obiba.agate.service;

import org.obiba.agate.domain.Group;
import org.obiba.agate.domain.User;
import org.obiba.agate.repository.GroupRepository;
import org.obiba.agate.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

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
  public Group findGroup(@NotNull String name) {
    Assert.notNull(name, "Group name cannot be null.");
    return groupRepository.findOneByName(name);
  }

  @Nullable
  public List<Group> findByApplication(@NotNull String application) {
    Assert.notNull(application, "Application name cannot be null.");
    return findByApplication(application);
  }

  public void ensureGroups(@NotNull Collection<String> names) {
    Assert.notNull(names, "Group names cannot be null.");
    names.forEach(this::getGroup);
  }

  /**
   * Get group with id and throws {@link org.obiba.agate.service.NoSuchGroupException} if not found.
   *
   * @param id
   * @return
   */
  public Group getGroup(String id) {
    Group group = groupRepository.findOne(id);
    if(group == null) throw NoSuchGroupException.withId(id);
    return group;
  }

  /**
   * Insert of update a {@link org.obiba.agate.domain.Group}.
   *
   * @param group
   * @return
   */
  public Group save(@NotNull Group group) {
    if(group.isNew()) {
      group.setNameAsId();
    }
    groupRepository.save(group);
    return group;
  }

  /**
   * Delete a {@link org.obiba.agate.domain.Group}.
   *
   * @param group
   */
  public void delete(@NotNull Group group) {
    for(User user : userRepository.findAll()) {
      if(user.getGroups().contains(group.getName())) {
        throw NotOrphanGroupException.withName(group.getName());
      }
    }

    groupRepository.delete(group);
  }

}

