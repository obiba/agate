package org.obiba.agate.domain;

import java.util.List;

import org.hibernate.validator.constraints.NotBlank;
import org.obiba.mongodb.domain.AbstractAuditableDocument;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Document
public class Configuration extends AbstractAuditableDocument {

  private static final long serialVersionUID = -9020464712632680519L;

  public static final String DEFAULT_NAME = "Agate";

  public static final int DEFAULT_SHORT_TIMEOUT = 8; // 8 hours

  public static final int DEFAULT_LONG_TIMEOUT = 24 * 30 * 3; // 3 months

  public static final int DEFAULT_INACTIVE_TIMEOUT = 365 * 24; // 1 year (in hours)

  @NotBlank
  private String name = DEFAULT_NAME;

  private String domain;

  private int shortTimeout = DEFAULT_SHORT_TIMEOUT;

  private int longTimeout = DEFAULT_LONG_TIMEOUT;

  private int inactiveTimeout = DEFAULT_INACTIVE_TIMEOUT;

  private String secretKey;

  private List<AttributeConfiguration> userAttributes;

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

  public boolean hasUserAttributes() {
    return userAttributes != null && userAttributes.size() > 0;
  }

  public List<AttributeConfiguration> getUserAttributes() {
    return userAttributes;
  }

  public void setUserAttributes(List<AttributeConfiguration> userAttributes) {
    this.userAttributes = userAttributes;
  }

  public void addUserAttribute(AttributeConfiguration config) {
    if(userAttributes == null) userAttributes = Lists.newArrayList();
    userAttributes.add(config);
  }

  @Override
  protected Objects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("name", name) //
      .add("domain", domain) //
      .add("shortTimeout", shortTimeout) //
      .add("longTimeout", longTimeout);
  }

  public int getInactiveTimeout() {
    return inactiveTimeout;
  }

  public void setInactiveTimeout(int inactiveTimeout) {
    this.inactiveTimeout = inactiveTimeout;
  }
}
