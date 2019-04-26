package org.obiba.agate.service;

import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.domain.RealmStatus;
import org.obiba.agate.domain.User;
import org.obiba.agate.repository.RealmConfigRepository;
import org.obiba.agate.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Transactional
public class RealmConfigService {

  private static final String[] IGNORE_PROPERTIES =
    {"id", "name", "version", "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"};

  private final RealmConfigRepository realmConfigRepository;

  private final UserRepository userRepository;

  private final GroupService groupService;

  @Inject
  public RealmConfigService(RealmConfigRepository realmConfigRepository,
                            UserRepository userService,
                            GroupService groupService) {
    this.realmConfigRepository = realmConfigRepository;
    this.userRepository = userService;
    this.groupService = groupService;
  }

  public RealmConfig save(@NotNull RealmConfig config) {
    assert config != null;

    groupService.ensureGroupsByName(config.getGroups());
    RealmConfig saved = config;

    if (saved.isNew()) {
      saved.setNameAsId();
    } else {
      saved = getConfig(config.getName());

      // only one can be default realm
      if (config.isDefaultRealm() != saved.isDefaultRealm() && config.isDefaultRealm()) {
        realmConfigRepository.findAll().stream().filter(realmConfig -> !realmConfig.getName().equals(config.getName())).forEach(realmConfig -> {
          realmConfig.setDefaultRealm(false);
          realmConfigRepository.save(realmConfig);
        });
      }

      BeanUtils.copyProperties(config, saved, IGNORE_PROPERTIES);
    }

    return realmConfigRepository.save(saved);
  }

  public List<RealmConfig> findAll() {
    return realmConfigRepository.findAll();
  }

  public RealmConfig findConfig(@NotNull String name) {
    Assert.notNull(name, "Realm config name cannot be null.");
    return realmConfigRepository.findOneByName(name);
  }

  public RealmConfig findDefault() {
    return realmConfigRepository.findOneByDefaultRealmTrue();
  }

  public RealmConfig getConfig(@NotNull String name) {
    Assert.notNull(name, "Realm config name cannot be null.");
    RealmConfig config = findConfig(name);
    if (config == null) throw NoSuchRealmConfigException.withName(name);
    return config;
  }

  public void delete(@NotNull String name) {
    Assert.notNull(name, "Realm config name cannot be null.");

    List<String> usernames = getUsernames(name);
    if (usernames.size() > 0) {
      throw NotOrphanRealmException.withName(name);
    }

    RealmConfig config = findConfig(name);
    if (config != null) realmConfigRepository.delete(config);
  }

  public void activate(@NotNull String name) {
    updateStatus(name, RealmStatus.ACTIVE);
  }

  public void deactivate(@NotNull String name) {
    updateStatus(name, RealmStatus.INACTIVE);
  }

  private void updateStatus(String name, RealmStatus status) {
    Assert.notNull(name, "Realm config name cannot be null.");
    RealmConfig config = getConfig(name);
    config.setStatus(status);
    realmConfigRepository.save(config);
  }

  public void updateGroups(@NotNull String name, @NotNull Collection<String> groups) {
    Assert.notNull(name, "Realm config name cannot be null.");
    groupService.ensureGroupsByName(groups);
    RealmConfig config = getConfig(name);
    config.setGroups(groups);
    realmConfigRepository.save(config);
  }

  public List<String> getUsernames(String name) {
    return userRepository.findAll().stream().map(User::getRealm).filter(name::equals).collect(Collectors.toList());
  }
}
