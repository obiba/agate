package org.obiba.agate.service;

import javax.inject.Inject;
import javax.validation.Valid;

import org.obiba.agate.domain.Configuration;
import org.obiba.agate.event.AgateConfigUpdatedEvent;
import org.obiba.agate.repository.AgateConfigRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;

@Component
public class ConfigurationService {

  @Inject
  private AgateConfigRepository agateConfigRepository;

  @Inject
  private EventBus eventBus;

  @Cacheable(value = "agateConfig", key = "#root.methodName")
  public Configuration getConfiguration() {
    return getOrCreateConfiguration();
  }

  private Configuration getOrCreateConfiguration() {
    if(agateConfigRepository.count() == 0) {
      Configuration configuration = new Configuration();
      agateConfigRepository.save(configuration);
      return getConfiguration();
    }
    return agateConfigRepository.findAll().get(0);
  }

  @CacheEvict(value = "agateConfig", allEntries = true)
  public void save(@Valid Configuration configuration) {
    Configuration savedConfiguration = getOrCreateConfiguration();
    BeanUtils.copyProperties(configuration, savedConfiguration, "id", "version", "createdBy", "createdDate", "lastModifiedBy",
        "lastModifiedDate");
    agateConfigRepository.save(savedConfiguration);
    eventBus.post(new AgateConfigUpdatedEvent(getConfiguration()));
  }

}
