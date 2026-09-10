package org.obiba.agate.security;

import org.apache.shiro.crypto.hash.Sha512Hash;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

  private final PasswordHasher hasher = new PasswordHasher("agate", 10);

  @Test
  void hashIsArgon2WithRandomSalt() {
    String h1 = hasher.hash("P@ssw0rd");
    String h2 = hasher.hash("P@ssw0rd");
    assertTrue(h1.startsWith("$shiro2$argon2id$"), h1);
    assertNotEquals(h1, h2);
    assertFalse(hasher.isLegacy(h1));
  }

  @Test
  void matchesArgon2Hash() {
    String h = hasher.hash("P@ssw0rd");
    assertTrue(hasher.matches("P@ssw0rd", h));
    assertFalse(hasher.matches("P@ssw0rd!", h));
  }

  @Test
  void matchesLegacyHash() {
    String legacy = new Sha512Hash("P@ssw0rd", "agate", 10).toString();
    assertTrue(hasher.isLegacy(legacy));
    assertTrue(hasher.matches("P@ssw0rd", legacy));
    assertFalse(hasher.matches("P@ssw0rd!", legacy));
    assertFalse(new PasswordHasher("other", 10).matches("P@ssw0rd", legacy));
  }

  @Test
  void rejectsMissingOrMalformed() {
    assertFalse(hasher.matches("x", null));
    assertFalse(hasher.matches("x", ""));
    assertFalse(hasher.matches(null, hasher.hash("x")));
    assertFalse(hasher.matches("x", "$shiro9$unknown$format"));
    assertFalse(hasher.isLegacy(null));
  }
}
