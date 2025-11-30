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
    assertFalse(config.appliesTo("PUT", "/user//password"));
    assertTrue(config.appliesTo("PUT", "/user/toto/password/"));
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

  @Test
  public void appliesToReturnsFalseForEmptyEndpointList() {
    ReAuthConfiguration config = new ReAuthConfiguration();
    config.setEndpoints(Lists.newArrayList());
    assertFalse(config.appliesTo("GET", "/any/path"));
  }

  @Test
  public void appliesToThrowsExceptionForInvalidEndpointFormat() {
    ReAuthConfiguration config = new ReAuthConfiguration();
    IllegalArgumentException exception =
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
          config.setEndpoints(Lists.newArrayList("INVALID-ENDPOINT"));
        });
    assertTrue(exception.getMessage().contains("Invalid endpoint format"));
  }

  @Test
  public void appliesToHandlesNullEndpointsGracefully() {
    ReAuthConfiguration config = new ReAuthConfiguration();
    config.setEndpoints(null);
    assertFalse(config.appliesTo("GET", "/any/path"));
  }

  @Test
  public void appliesToHandlesMultipleWildcardsCorrectly() {
    ReAuthConfiguration config = new ReAuthConfiguration();
    config.setEndpoints(Lists.newArrayList("GET:/user/*/profile/*"));
    assertTrue(config.appliesTo("GET", "/user/123/profile/456"));
    assertFalse(config.appliesTo("GET", "/user/123/profile"));
    assertFalse(config.appliesTo("POST", "/user/123/profile/456"));
  }

  @Test
  public void appliesToHandlesCaseSensitivityInMethod() {
    ReAuthConfiguration config = new ReAuthConfiguration();
    config.setEndpoints(Lists.newArrayList("get:/users"));
    assertTrue(config.appliesTo("GET", "/users"));
    assertTrue(config.appliesTo("get", "/users"));
  }

}
