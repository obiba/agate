package org.obiba.agate.domain;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationTest {

  @AfterEach
  void resetClock() {
    DateTimeUtils.setCurrentMillisSystem();
  }

  @Test
  void freshCodeIsUnusedAndNotExpired() {
    Authorization authorization = new Authorization("user", "app");
    authorization.setCode("abc");
    assertTrue(authorization.hasCode());
    assertFalse(authorization.isCodeUsed());
    assertNotNull(authorization.getCodeCreatedDate());
    assertFalse(authorization.isCodeExpired(300));
  }

  @Test
  void codeExpiresAfterLifetime() {
    DateTimeUtils.setCurrentMillisFixed(DateTime.now().getMillis());
    Authorization authorization = new Authorization("user", "app");
    authorization.setCode("abc");
    DateTimeUtils.setCurrentMillisFixed(DateTime.now().plusSeconds(301).getMillis());
    assertTrue(authorization.isCodeExpired(300));
  }

  @Test
  void codeWithoutIssueDateIsExpired() {
    // documents persisted before the issue date was recorded
    Authorization authorization = new Authorization("user", "app");
    assertFalse(authorization.hasCode());
    assertTrue(authorization.isCodeExpired(300));
  }

  @Test
  void usedCodeIsResetByNewCode() {
    Authorization authorization = new Authorization("user", "app");
    authorization.setCode("abc");
    authorization.useCode();
    assertTrue(authorization.isCodeUsed());
    authorization.setCode("def");
    assertFalse(authorization.isCodeUsed());
    assertEquals("def", authorization.getCode());
  }
}
