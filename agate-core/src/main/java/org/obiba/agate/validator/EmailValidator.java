package org.obiba.agate.validator;

import jakarta.mail.internet.InternetAddress;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

public final class EmailValidator {

  public static boolean isValid(String email) {
    if (email == null || email.trim().isEmpty()) {
      return false;
    }

    // Check length constraints
    if (email.length() > 254) {
      return false; // Email too long
    }

    String[] parts = email.split("@");
    if (parts[0].length() > 64) {
      return false; // Local part too long
    }

    // Check for common issues
    if (email.contains("..") || email.startsWith(".") || email.endsWith(".")) {
      return false; // Invalid dot placement
    }

    try {
      InternetAddress emailAddr = new InternetAddress(email);
      emailAddr.validate();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
