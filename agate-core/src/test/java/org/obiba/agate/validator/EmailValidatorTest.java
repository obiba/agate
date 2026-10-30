package org.obiba.agate.validator;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {

  @Test
  void isValidWhenAddress() {
    for (String email : new String[] { "bob@example.org", "bob.smith+agate@example.org", "o'brien@example.org",
      "a&b@example.org", "bob@localhost", "\"bob\"@example.org", "bob@[1.2.3.4]" }) {
      assertTrue(EmailValidator.isValid(email), email);
    }
  }

  @Test
  void isNotValidWhenMalformed() {
    for (String email : Arrays.asList(null, "", "   ", "bob", "@", "@example.org", "bob@", "bob@@example.org",
      "bob smith@example.org", " bob@example.org", "bob@example.org ", "bob..smith@example.org", ".bob@example.org",
      "bob@example.org.", "a".repeat(65) + "@example.org")) {
      assertFalse(EmailValidator.isValid(email), email);
    }
  }

  @Test
  void isNotValidWhenMarkupNameOrList() {
    for (String email : new String[] { "<img src=x onerror=alert(1)>@x.org", "\"<b>bob</b>\"@x.org",
      "Bob <bob@x.org>", "bob@x.org (Bob)", "bob@x.org, eve@x.org" }) {
      assertFalse(EmailValidator.isValid(email), email);
    }
  }
}
