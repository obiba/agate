package org.obiba.agate.domain;

public enum AgateRealm {
  USER_REALM("agate-user-realm"),
  TOKEN_REALM("agate-token-realm"),
  LDAP_REALM("agate-ldap-realm");

  private final String name;

  AgateRealm(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
