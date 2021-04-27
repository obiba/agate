/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.domain;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Iterables;
import org.hibernate.validator.constraints.NotBlank;
import org.obiba.mongodb.domain.AbstractAuditableDocument;
import org.obiba.runtime.Version;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Document
public class Configuration extends AbstractAuditableDocument {

  private static final long serialVersionUID = -9020464712632680519L;

  public static final String DEFAULT_NAME = "Agate";

  public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

  public static final int DEFAULT_SHORT_TIMEOUT = 8; // 8 hours

  public static final int DEFAULT_LONG_TIMEOUT = 24 * 30 * 3; // 3 months

  public static final int DEFAULT_INACTIVE_TIMEOUT = 365 * 24; // 1 year (in hours)

  @NotBlank
  private String name = DEFAULT_NAME;

  private String domain;

  private String publicUrl;

  private String portalUrl;

  private int shortTimeout = DEFAULT_SHORT_TIMEOUT;

  private int longTimeout = DEFAULT_LONG_TIMEOUT;

  private int inactiveTimeout = DEFAULT_INACTIVE_TIMEOUT;

  private String secretKey;

  private Version agateVersion;

  private boolean joinPageEnabled = true;

  private boolean joinWithUsername = true;

  private List<AttributeConfiguration> userAttributes;

  private String style;

  private LocalizedString translations;

  private List<Locale> locales = Lists.newArrayList();

  @Transient
  private String contextPath;

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

  public boolean hasPublicUrl() {
    return !Strings.isNullOrEmpty(publicUrl);
  }

  public String getPublicUrl() {
    if (!Strings.isNullOrEmpty(publicUrl) && publicUrl.endsWith("/")) {
      return publicUrl.substring(0, publicUrl.length() -1);
    }
    return publicUrl;
  }

  public void setPublicUrl(String publicUrl) {
    this.publicUrl = publicUrl;
  }

  public boolean hasPortalUrl() {
    return !Strings.isNullOrEmpty(portalUrl);
  }

  public String getPortalUrl() {
    return portalUrl;
  }

  public void setPortalUrl(String portalUrl) {
    this.portalUrl = portalUrl;
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

  public boolean isJoinPageEnabled() {
    return joinPageEnabled;
  }

  public void setJoinPageEnabled(boolean joinPageEnabled) {
    this.joinPageEnabled = joinPageEnabled;
  }

  public boolean isJoinWithUsername() {
    return joinWithUsername;
  }

  public void setJoinWithUsername(boolean joinWithUsername) {
    this.joinWithUsername = joinWithUsername;
  }

  public boolean hasUserAttributes() {
    return userAttributes != null && userAttributes.size() > 0;
  }

  public List<AttributeConfiguration> getUserAttributes() {
    return userAttributes == null ? userAttributes = Lists.newArrayList() : userAttributes;
  }

  public void setUserAttributes(List<AttributeConfiguration> userAttributes) {
    this.userAttributes = userAttributes;
  }

  public void addUserAttribute(AttributeConfiguration config) {
    if(userAttributes == null) userAttributes = Lists.newArrayList();
    userAttributes.add(config);
  }

  public int getInactiveTimeout() {
    return inactiveTimeout;
  }

  public void setInactiveTimeout(int inactiveTimeout) {
    this.inactiveTimeout = inactiveTimeout;
  }

  public void setAgateVersion(Version agateVersion) {
    this.agateVersion = agateVersion;
  }

  public Version getAgateVersion() {
    return agateVersion;
  }

  public boolean hasStyle() {
    return !Strings.isNullOrEmpty(style);
  }

  public void setStyle(String style) {
    this.style = style;
  }

  public String getStyle() {
    return style;
  }

  public List<Locale> getLocales() {
    return locales == null ? (locales = Lists.newArrayList()) : locales;
  }

  public List<String> getLocalesAsString() {
    List<String> list = Lists.newArrayList(Iterables.transform(getLocales(), Locale::getLanguage));
    Collections.sort(list);
    return list;
  }

  public void setLocales(List<Locale> locales) {
    this.locales = locales;
  }

  public LocalizedString getTranslations() {
    return translations;
  }

  public void setTranslations(LocalizedString translations) {
    this.translations = translations;
  }

  public boolean hasTranslations() {
    return translations != null && !translations.isEmpty();
  }

  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  public String getContextPath() {
    return contextPath;
  }
}
