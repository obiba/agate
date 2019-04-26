package org.obiba.agate.service.support;

public class LdapRealmConfigFormBuilder extends BaseRealmConfigFormBuilder {

  private LdapRealmConfigFormBuilder() {
    formSchema = "{" +
      "  \"type\": \"object\"," +
      "  \"properties\": {" +
      "    \"url\": {" +
      "      \"type\": \"string\"," +
      "      \"title\": \"t(realm.ldap.url)\"" +
      "    }," +
      "    \"systemUser\": {" +
      "      \"type\": \"string\"," +
      "      \"title\": \"t(realm.ldap.system-username)\"" +
      "    }," +
      "    \"systemPassword\": {" +
      "      \"type\": \"string\"," +
      "      \"title\": \"t(realm.ldap.system-password)\"," +
      "      \"format\": \"password\"" +
      "    }," +
      "    \"userDnTemplate\": {" +
      "      \"type\": \"string\"," +
      "      \"title\": \"t(realm.ldap.user-dn-template)\"," +
      "      \"description\": \"t(realm.ldap.user-dn-template-help)\"" +
      "    }" +
      "  }," +
      "  \"required\": [" +
      "    \"url\"," +
      "    \"systemUser\"," +
      "    \"systemPassword\"," +
      "    \"userDnTemplate\"" +
      "  ]" +
      "}";

    formDefinition = "[" +
      "{" +
      "  \"type\": \"section\"," +
      "  \"items\": [" +
      "      \"url\"," +
      "      \"systemUser\"," +
      "      {" +
      "        \"key\": \"systemPassword\"," +
      "        \"type\": \"password\"" +
      "      }," +
      "      {" +
      "        \"type\": \"section\"," +
      "        \"items\": [" +
      "          {" +
      "            \"key\": \"userDnTemplate\"," +
      "            \"placeholder\": \"uid={0},ou=People,dc=example,dc=com\"" +
      "          }" +
      "        ]" +
      "      }" +
      "    ]" +
      "  }" +
      "]";
  }

  public static LdapRealmConfigFormBuilder newBuilder() {
    return new LdapRealmConfigFormBuilder();
  }
}
