package org.obiba.agate.domain;

public enum Enforced2FAStrategy {
  NONE, // 2FA not enforced
  APP, // authenticator app only
  ANY  // authenticator app or email
}
