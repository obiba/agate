package org.obiba.agate.service;

import com.google.common.eventbus.EventBus;
import org.obiba.agate.domain.AgateRealm;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.domain.RealmStatus;
import org.obiba.agate.domain.User;
import org.obiba.agate.event.RealmConfigActivatedOrUpdatedEvent;
import org.obiba.agate.event.RealmConfigDeactivatedEvent;
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

  private final EventBus eventBus;

  @Inject
  public RealmConfigService(
    RealmConfigRepository realmConfigRepository,
    UserRepository userRepository,
    GroupService groupService,
    EventBus eventBus) {
    this.realmConfigRepository = realmConfigRepository;
    this.userRepository = userRepository;
    this.groupService = groupService;
    this.eventBus = eventBus;
  }

  public RealmConfig save(@NotNull RealmConfig config) {
    assert config != null;

    groupService.ensureGroupsByName(config.getGroups());
    updateConfigForStatus(config);
    RealmConfig saved = config;

    boolean aNew = saved.isNew();

    if (aNew) {
      saved.setNameAsId();
    } else {
      saved = getConfig(config.getName());

      // only one can be default realm
      if (config.isDefaultRealm() != saved.isDefaultRealm() && config.isDefaultRealm()) {
        realmConfigRepository.findAll()
          .stream()
          .filter(realmConfig -> !realmConfig.getName().equals(config.getName()))
          .forEach(realmConfig -> {
            realmConfig.setDefaultRealm(false);
            realmConfigRepository.save(realmConfig);
          });
      }

      BeanUtils.copyProperties(config, saved, IGNORE_PROPERTIES);
    }

    RealmConfig savedRealmConfig = realmConfigRepository.save(saved);

    if (RealmStatus.ACTIVE.equals(savedRealmConfig.getStatus())) {
      eventBus.post(new RealmConfigActivatedOrUpdatedEvent(savedRealmConfig));
    } else if (RealmStatus.INACTIVE.equals(savedRealmConfig.getStatus())) {
      eventBus.post(new RealmConfigDeactivatedEvent(config));
    }

    return savedRealmConfig;
  }

  private void updateConfigForStatus(RealmConfig config) {
    boolean enable = RealmStatus.ACTIVE.equals(config.getStatus());
    config.setForSignup(enable && config.isForSignup());
    config.setDefaultRealm(enable && config.isDefaultRealm());
  }

  public List<RealmConfig> findAll() {
    return realmConfigRepository.findAll();
  }

  public List<RealmConfig> findAllRealmsForSignup() {
    return realmConfigRepository.findAllByStatusAndForSignupTrue(RealmStatus.ACTIVE);
  }

  public List<RealmConfig> findAllByStatus(RealmStatus status) {
    return realmConfigRepository.findAllByStatus(status);
  }

  public List<RealmConfig> findAllForSignupByStatusAndType(RealmStatus status, AgateRealm agateRealm) {
    return realmConfigRepository.findAllByStatusAndTypeAndForSignupTrue(status, agateRealm.name());
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
    if (config != null)  {
      realmConfigRepository.delete(config);
      eventBus.post(new RealmConfigDeactivatedEvent(config));
    }
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

    if (RealmStatus.ACTIVE.equals(status)) {
      eventBus.post(new RealmConfigActivatedOrUpdatedEvent(config));
    } else {
      eventBus.post(new RealmConfigDeactivatedEvent(config));
    }
  }

  public void updateGroups(@NotNull String name, @NotNull Collection<String> groups) {
    Assert.notNull(name, "Realm config name cannot be null.");
    groupService.ensureGroupsByName(groups);
    RealmConfig config = getConfig(name);
    config.setGroups(groups);
    realmConfigRepository.save(config);
  }

  public List<User> getUsers(String name) {
    return userRepository.findAll().stream().filter(user -> user.getRealm().equals(name)).collect(Collectors.toList());
  }

  public List<String> getUsernames(String name) {
    return userRepository.findAll().stream().map(User::getRealm).filter(name::equals).collect(Collectors.toList());
  }
}
