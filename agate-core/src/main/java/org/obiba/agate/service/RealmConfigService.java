package org.obiba.agate.service;

import org.jvnet.hk2.annotations.Service;
import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.repository.RealmConfigRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

@Service
@Transactional
public class RealmConfigService {

  private RealmConfigRepository realmConfigRepository;

  @Inject
  public RealmConfigService(RealmConfigRepository realmConfigRepository) {
    this.realmConfigRepository = realmConfigRepository;
  }

  public RealmConfig save(@NotNull RealmConfig config) {
    return realmConfigRepository.save(config);
  }

  public RealmConfig findConfig(@NotNull String name) {
    return realmConfigRepository.findOneByName(name);
  }

}
