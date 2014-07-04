package org.obiba.agate.domain;

import org.hibernate.validator.constraints.NotBlank;
import org.obiba.mongodb.domain.AbstractAuditableDocument;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

@Document
public class Configuration extends AbstractAuditableDocument {

  private static final long serialVersionUID = -9020464712632680519L;

  public static final String DEFAULT_NAME = "Agate";

  public static final int DEFAULT_SHORT_TIMEOUT = 8; // 8 hours

  public static final int DEFAULT_LONG_TIMEOUT = 24*30*3; // 3 months

  @NotBlank
  private String name = DEFAULT_NAME;

  private String domain;

  private int shortTimeout = DEFAULT_SHORT_TIMEOUT;

  private int longTimeout = DEFAULT_LONG_TIMEOUT;

  private String secretKey;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public boolean hasDomain() {
    return !Strings.isNullOrEmpty(domain);
  }

  public int getShortTimeout() {
    return shortTimeout;
  }

  public void setShortTimeout(int shortTimeout) {
    this.shortTimeout = shortTimeout;
  }

  public int getLongTimeout() {
    return longTimeout;
  }

  public void setLongTimeout(int longTimeout) {
    this.longTimeout = longTimeout;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  @Override
  protected Objects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("name", name) //
        .add("domain", domain) //
        .add("shortTimeout", shortTimeout) //
        .add("longTimeout", longTimeout);
  }
}
