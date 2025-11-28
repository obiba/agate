package org.obiba.agate.config;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReAuthConfigurationTest {

  @Test
  public void testExactMatch() {
    ReAuthConfiguration config = new ReAuthConfiguration();
    config.setEndpoints(Lists.newArrayList("POST:/users"));
    assertTrue(config.appliesTo("POST", "/users"));
    assertFalse(config.appliesTo("GET", "/users"));
  }

  @Test
  public void testWildcardMatch() {
    ReAuthConfiguration config = new ReAuthConfiguration();
    config.setEndpoints(Lists.newArrayList("PUT:/user/*/password"));
    assertFalse(config.appliesTo("PUT", "/user/toto"));
    assertTrue(config.appliesTo("PUT", "/user/toto/password"));
  }

  @Test
  public void testEndingWildcardMatch() {
    ReAuthConfiguration config = new ReAuthConfiguration();
    config.setEndpoints(Lists.newArrayList("PUT:/user/*"));
    assertTrue(config.appliesTo("PUT", "/user/toto"));
    assertFalse(config.appliesTo("PUT", "/user/toto/password"));
  }

  @Test
  public void testWildcardsMatch() {
    ReAuthConfiguration config = new ReAuthConfiguration();
    config.setEndpoints(Lists.newArrayList("PUT:/user/*/attribute/*"));
    assertFalse(config.appliesTo("PUT", "/user/toto/password"));
    assertFalse(config.appliesTo("PUT", "/user/toto/attribute"));
    assertTrue(config.appliesTo("PUT", "/user/toto/attribute/value"));
  }
}
