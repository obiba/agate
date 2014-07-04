package org.obiba.agate.service;

import java.security.Key;

import javax.inject.Inject;
import javax.validation.Valid;

import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.util.ByteSource;
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

  private final AesCipherService cipherService = new AesCipherService();

  @Cacheable(value = "agateConfig", key = "#root.methodName")
  public Configuration getConfiguration() {
    return getOrCreateConfiguration();
  }

  private Configuration getOrCreateConfiguration() {
    if(agateConfigRepository.count() == 0) {
      Configuration configuration = new Configuration();
      configuration.setSecretKey(generateSecretKey());
      agateConfigRepository.save(configuration);
      return getConfiguration();
    }
    return agateConfigRepository.findAll().get(0);
  }

  @CacheEvict(value = "agateConfig", allEntries = true)
  public void save(@Valid Configuration configuration) {
    Configuration savedConfiguration = getOrCreateConfiguration();
    BeanUtils.copyProperties(configuration, savedConfiguration, "id", "version", "createdBy", "createdDate",
        "lastModifiedBy", "lastModifiedDate", "secretKey");
    agateConfigRepository.save(savedConfiguration);
    eventBus.post(new AgateConfigUpdatedEvent(getConfiguration()));
  }

  public String encrypt(String plain) {
    ByteSource encrypted = cipherService.encrypt(CodecSupport.toBytes(plain), getSecretKey());
    return encrypted.toHex();
  }

  public String decrypt(String encrypted) {
    ByteSource decrypted = cipherService.decrypt(Hex.decode(encrypted), getSecretKey());
    return CodecSupport.toString(decrypted.getBytes());
  }

  private String generateSecretKey() {
    Key key = cipherService.generateNewKey();
    return Hex.encodeToString(key.getEncoded());
  }

  private byte[] getSecretKey() {
    return Hex.decode(getOrCreateConfiguration().getSecretKey());
  }
}
