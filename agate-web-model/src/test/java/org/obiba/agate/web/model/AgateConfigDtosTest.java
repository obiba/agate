package org.obiba.agate.web.model;

import java.util.Locale;

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
    config.setPublicUrl("http://localhost/agate-test");
    config.setDefaultCharacterSet("utf-8");
    config.getLocales().add(Locale.CHINESE);
    config.getLocales().add(Locale.GERMAN);

    Agate.AgateConfigDto dto = dtos.asDto(config);
    assertThat(dto).isNotNull();

    AgateConfig fromDto = dtos.fromDto(dto);
    assertThat(fromDto).isEqualToIgnoringGivenFields(config, "createdDate");
  }

}
