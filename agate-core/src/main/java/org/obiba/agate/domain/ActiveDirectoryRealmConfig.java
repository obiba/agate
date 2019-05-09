package org.obiba.agate.domain;

import org.json.JSONException;
import org.json.JSONObject;

public class ActiveDirectoryRealmConfig {

  private static final String URL_FIELD = "url";
  private static final String SYSTEM_USERNAME_FIELD = "systemUsername";
  private static final String SYSTEM_PASSWORD_FIELD = "systemPassword";
  private static final String SEARCH_FILTER_FIELD = "searchFilter";
  private static final String SEARCH_BASE_FIELD = "searchBase";
  private static final String PRINCIPAL_SUFFIX_FIELD = "principalSuffix";


  private ActiveDirectoryRealmConfig() { }

  private String url;

  private String systemUsername;

  private String systemPassword;

  private String searchFilter;

  private String searchBase;

  private String principalSuffix;

  public String getUrl() {
    return url;
  }

  public String getSystemUsername() {
    return systemUsername;
  }

  public String getSystemPassword() {
    return systemPassword;
  }

  public String getSearchFilter() {
    return searchFilter;
  }

  public String getSearchBase() {
    return searchBase;
  }

  public String getPrincipalSuffix() {
    return principalSuffix;
  }

  public JSONObject getAsSecuredJSONObject() throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(URL_FIELD, url);
    jsonObject.put(SYSTEM_USERNAME_FIELD, systemUsername);
    jsonObject.put(SYSTEM_PASSWORD_FIELD, "");
    jsonObject.put(SEARCH_FILTER_FIELD, searchFilter);
    jsonObject.put(SEARCH_BASE_FIELD, searchBase);
    jsonObject.put(PRINCIPAL_SUFFIX_FIELD, principalSuffix);

    return jsonObject;
  }

  public static Builder newBuilder(String content) throws JSONException {
    return newBuilder(new JSONObject(content));
  }

  public static ActiveDirectoryRealmConfig.Builder newBuilder(JSONObject content) {
    return new Builder(content);
  }

  public static class Builder {
    private final ActiveDirectoryRealmConfig config;

    private Builder(JSONObject content) {
      config = new ActiveDirectoryRealmConfig();
      config.url = content.optString(URL_FIELD);
      config.systemUsername = content.optString(SYSTEM_USERNAME_FIELD);
      config.systemPassword = content.optString(SYSTEM_PASSWORD_FIELD);
      config.searchFilter = content.optString(SEARCH_FILTER_FIELD);
      config.searchBase = content.optString(SEARCH_BASE_FIELD);
      config.principalSuffix = content.optString(PRINCIPAL_SUFFIX_FIELD);
    }

    public ActiveDirectoryRealmConfig build() {
      return config;
    }
  }
}
