package org.obiba.agate.validator;

import java.util.regex.Pattern;

public final class NameValidator {

  // International pattern supporting accents and other characters
  private static final Pattern INTERNATIONAL_NAME_PATTERN = Pattern.compile(
      "^[\\p{L}][\\p{L}\\s\\-'_\\.0-9@]{1,49}$");

  public static boolean isValid(String name) {
    if (name == null || name.isEmpty()) {
      return true;
    }

    return INTERNATIONAL_NAME_PATTERN.matcher(name).matches();
  }
}
