package org.obiba.agate.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import jakarta.annotation.Nonnull;
import org.obiba.agate.domain.*;
import org.obiba.agate.event.RealmConfigActivatedOrUpdatedEvent;
import org.obiba.agate.event.RealmConfigDeactivatedEvent;
import org.obiba.agate.repository.RealmConfigRepository;
import org.obiba.agate.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.inject.Inject;
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

  public RealmConfig save(@Nonnull RealmConfig config) {
    assert config != null;

    groupService.ensureGroupsByName(config.getGroups());
    updateConfigForStatus(config);
    RealmConfig saved = config;

    boolean aNew = saved.isNew();

    if (aNew) {
      saved.setNameAsId();
    } else {
      saved = getConfig(config.getName());

      realmConfigRepository.findAll()
        .stream()
        .filter(realmConfig -> !realmConfig.getName().equals(config.getName()))
        .forEach(realmConfig -> {
          realmConfigRepository.save(realmConfig);
        });

      BeanUtils.copyProperties(config, saved, IGNORE_PROPERTIES);
    }

    RealmConfig savedRealmConfig;
    if (aNew)
      savedRealmConfig = realmConfigRepository.insert(saved);
    else
      savedRealmConfig = realmConfigRepository.save(saved);

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
  }

  public List<RealmConfig> findAll() {
    return realmConfigRepository.findAll();
  }

  public List<RealmConfig> findAllRealmsForSignup() {
    return realmConfigRepository.findAllByStatusAndForSignupTrue(RealmStatus.ACTIVE);
  }

  public List<RealmConfig> findAllRealmsForSignupAndApplication(String application) {
    if (Strings.isNullOrEmpty(application)) return Lists.newArrayList();
    List<String> groupsForAppication = groupService.findByApplication(application)
      .stream()
      .map(Group::getName)
      .collect(Collectors.toList());

    return realmConfigRepository.findAllByStatusAndForSignupTrue(RealmStatus.ACTIVE)
      .stream()
      .filter(realmConfig -> realmConfig.getGroups().stream().anyMatch(groupsForAppication::contains))
      .collect(Collectors.toList());
  }

  public List<RealmConfig> findAllByStatus(RealmStatus status) {
    return realmConfigRepository.findAllByStatus(status);
  }

  public List<RealmConfig> findAllForSignupByStatusAndType(RealmStatus status, AgateRealm agateRealm) {
    return realmConfigRepository.findAllByStatusAndTypeAndForSignupTrue(status, agateRealm.name());
  }

  public List<RealmConfig> findAllForUsageByStatusAndType(RealmUsage usage, RealmStatus status, AgateRealm agateRealm) {
    return RealmUsage.SIGNUP.equals(usage)
      ? realmConfigRepository.findAllByStatusAndTypeAndForSignupTrue(status, agateRealm.name())
      : realmConfigRepository.findAllByStatusAndType(status, agateRealm.name());
  }


  public List<RealmConfig> findAllForUsageByStatusAndTypeAndApplication(RealmUsage usage,
                                                                        RealmStatus status,
                                                                        AgateRealm agateRealm,
                                                                        String application) {

    if (Strings.isNullOrEmpty(application)) return Lists.newArrayList();
    List<String> groupsForAppication = groupService.findByApplication(application)
      .stream()
      .map(Group::getName)
      .collect(Collectors.toList());

    List<RealmConfig> realmConfigs = RealmUsage.ALL == usage
      ? realmConfigRepository.findAllByStatusAndType(status, agateRealm.name())
      : realmConfigRepository.findAllByStatusAndTypeAndForSignupTrue(status, agateRealm.name());

    return realmConfigs.stream()
      .filter(realmConfig -> realmConfig.getGroups().stream().anyMatch(groupsForAppication::contains))
      .collect(Collectors.toList());
  }

  public RealmConfig findConfig(@Nonnull String name) {
    Assert.notNull(name, "Realm config name cannot be null.");
    return realmConfigRepository.findOneByName(name);
  }

  public RealmConfig getConfig(@Nonnull String name) {
    Assert.notNull(name, "Realm config name cannot be null.");
    RealmConfig config = findConfig(name);
    if (config == null) throw NoSuchRealmConfigException.withName(name);
    return config;
  }

  public void delete(@Nonnull String name) {
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

  public void activate(@Nonnull String name) {
    updateStatus(name, RealmStatus.ACTIVE);
  }

  public void deactivate(@Nonnull String name) {
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

  public void updateGroups(@Nonnull String name, @Nonnull Collection<String> groups) {
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
