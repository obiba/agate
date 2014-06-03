package org.obiba.agate.web.model;

import org.junit.Test;
import org.obiba.agate.domain.AgateConfig;

import static org.assertj.core.api.Assertions.assertThat;

public class AgateConfigDtosTest {

  private final AgateConfigDtos dtos = new AgateConfigDtos();

  @Test
  public void test_default_values() {
    AgateConfig config = new AgateConfig();

    Agate.AgateConfigDto dto = dtos.asDto(config);
    assertThat(dto).isNotNull();

    AgateConfig fromDto = dtos.fromDto(dto);
    assertThat(fromDto).isEqualToIgnoringGivenFields(config, "createdDate");
  }

  @Test
  public void test_with_values() {
    AgateConfig config = new AgateConfig();
    config.setName("Test");
    config.setDomain("example.com");
    config.setPublicUrl("http://localhost/agate-test");

    Agate.AgateConfigDto dto = dtos.asDto(config);
    assertThat(dto).isNotNull();

    AgateConfig fromDto = dtos.fromDto(dto);
    assertThat(fromDto).isEqualToIgnoringGivenFields(config, "createdDate");
  }

}
