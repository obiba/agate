package org.obiba.agate.service;

import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.domain.RealmStatus;
import org.obiba.agate.repository.RealmConfigRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

@Component
@Transactional
public class RealmConfigService {

  private static final String[] IGNORE_PROPERTIES =
    {"id", "name", "version", "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"};

  private final RealmConfigRepository realmConfigRepository;

  @Inject
  public RealmConfigService(RealmConfigRepository realmConfigRepository) {
    this.realmConfigRepository = realmConfigRepository;
  }

  public RealmConfig save(@NotNull RealmConfig config) {
    assert config != null;
    RealmConfig saved = config;

    if (saved.isNew()) {
      saved.setId(RealmConfig.generateId());
    } else {
      saved = getConfig(config.getName());
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

  public RealmConfig getConfig(@NotNull String name) {
    Assert.notNull(name, "Realm config name cannot be null.");
    RealmConfig config = findConfig(name);
    if (config == null) throw NoSuchRealmConfigException.withName(name);
    return config;
  }

  public void delete(@NotNull String name) {
    Assert.notNull(name, "Realm config name cannot be null.");
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
    RealmConfig config = getConfig(name);
    config.setGroups(groups);
    realmConfigRepository.save(config);
  }

}
