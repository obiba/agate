package org.obiba.agate.service;

import javax.inject.Inject;
import javax.validation.Valid;

import org.obiba.agate.domain.AgateConfig;
import org.obiba.agate.event.AgateConfigUpdatedEvent;
import org.obiba.agate.repository.AgateConfigRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;

@Component
public class AgateConfigService {

  @Inject
  private AgateConfigRepository agateConfigRepository;

  @Inject
  private EventBus eventBus;

  @Cacheable(value = "agateConfig", key = "#root.methodName")
  public AgateConfig getConfig() {
    return getOrCreateMicaConfig();
  }

  private AgateConfig getOrCreateMicaConfig() {
    if(agateConfigRepository.count() == 0) {
      AgateConfig agateConfig = new AgateConfig();
      agateConfig.getLocales().add(AgateConfig.DEFAULT_LOCALE);
      agateConfigRepository.save(agateConfig);
      return getConfig();
    }
    return agateConfigRepository.findAll().get(0);
  }

  @CacheEvict(value = "agateConfig", allEntries = true)
  public void save(@Valid AgateConfig agateConfig) {
    AgateConfig savedConfig = getOrCreateMicaConfig();
    BeanUtils.copyProperties(agateConfig, savedConfig, "id", "version", "createdBy", "createdDate", "lastModifiedBy",
        "lastModifiedDate");
    agateConfigRepository.save(savedConfig);
    eventBus.post(new AgateConfigUpdatedEvent(getConfig()));
  }

}
