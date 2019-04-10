package org.obiba.agate.service;

import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.repository.RealmConfigRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.List;

@Component
@Transactional
public class RealmConfigService {

  private RealmConfigRepository realmConfigRepository;

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
      RealmConfig.mergePropeties(saved, config);
    }

    return realmConfigRepository.save(config);
  }

  public List<RealmConfig> findAll() {
    return realmConfigRepository.findAll();
  }

  public RealmConfig findConfig(@NotNull String name) {
    return realmConfigRepository.findOneByName(name);
  }

  public RealmConfig getConfig(@NotNull String name) {
    assert name != null;

    RealmConfig config = findConfig(name);
    if (config == null) throw NoSuchRealmConfigException.withName(name);
    return config;
  }

  public void delete(@NotNull RealmConfig config) {
    assert config != null;

    realmConfigRepository.delete(config);
  }
}
