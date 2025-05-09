package org.obiba.agate.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameValidatorTest {

  @Test
  void isValidWhenEmpty() {
    assertTrue(NameValidator.isValid(null));
    assertTrue(NameValidator.isValid(""));
  }

  @Test
  void isValidWhenNumbers() {
    assertTrue(NameValidator.isValid("user123"));
  }

  @Test
  void isValidWhenCases() {
    assertTrue(NameValidator.isValid("userONE"));
  }

  @Test
  void isValidWhenSpace() {
    assertTrue(NameValidator.isValid("user one"));
  }


  @Test
  void isValidWhenDot() {
    assertTrue(NameValidator.isValid("user.one"));
  }

  @Test
  void isValidWhenEmail() {
    assertTrue(NameValidator.isValid("user.one@me.com"));
    assertTrue(NameValidator.isValid("user.one@me-too.org"));
  }

  @Test
  void isInValidWhenSpecialCharacters() {
    assertFalse(NameValidator.isValid("user$"));
  }

  @Test
  void isInValidWhenTooLong() {
    assertFalse(NameValidator.isValid("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"));
  }
}