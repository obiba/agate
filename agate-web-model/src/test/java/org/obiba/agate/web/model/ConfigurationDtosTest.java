package org.obiba.agate.web.model;

import org.junit.Test;
import org.obiba.agate.domain.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigurationDtosTest {

  private final ConfigurationDtos dtos = new ConfigurationDtos();

  @Test
  public void test_default_values() {
    Configuration configuration = new Configuration();

    Agate.ConfigurationDto dto = dtos.asDto(configuration);
    assertThat(dto).isNotNull();

    Configuration fromDto = dtos.fromDto(dto);
    assertThat(fromDto).isEqualToIgnoringGivenFields(configuration, "createdDate");
  }

  @Test
  public void test_with_values() {
    Configuration configuration = new Configuration();
    configuration.setName("Test");
    configuration.setDomain("example.com");

    Agate.ConfigurationDto dto = dtos.asDto(configuration);
    assertThat(dto).isNotNull();

    Configuration fromDto = dtos.fromDto(dto);
    assertThat(fromDto).isEqualToIgnoringGivenFields(configuration, "createdDate");
  }

}
