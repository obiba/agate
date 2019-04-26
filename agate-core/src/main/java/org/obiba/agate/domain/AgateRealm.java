package org.obiba.agate.domain;

public enum AgateRealm {
  AGATE_USER_REALM("agate-user-realm"),
  AGATE_TOKEN_REALM("agate-token-realm"),
  AGATE_LDAP_REALM("agate-ldap-realm"),
  AGATE_JDBC_REALM("agate-jdbc-realm"),
  AGATE_AD_REALM("agate-ad-realm");

  private final String name;

  AgateRealm(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static AgateRealm fromString(String name) {
    return valueOf(name.replaceAll("-","_").toUpperCase());
  }
}
