package org.obiba.agate.security;

import com.google.common.base.Strings;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.service.ConfigurationService;
import org.obiba.agate.service.TotpService;
import org.obiba.shiro.NoSuchOtpException;
import org.obiba.shiro.authc.UsernamePasswordOtpToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AgateIniRealm extends IniRealm {

  private final Logger logger = LoggerFactory.getLogger(AgateIniRealm.class);

  public static final String INI_REALM = "agate-ini-realm";

  private static final String SHIRO2_PREFIX = "$shiro2$";

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private TotpService totpService;

  @Override
  public String getName() {
    return INI_REALM;
  }

  public AgateIniRealm() {
    super("classpath:shiro.ini");
    this.setPermissionResolver(new AgatePermissionResolver());
    this.setCredentialsMatcher(new PasswordMatcher());
  }

  /**
   * Overrides the default user definition processing to handle Shiro 2's Argon2 hash format.
   * <p>
   * The Argon2 crypt format contains commas in the parameters section
   * (e.g. {@code $shiro2$argon2id$v=19$t=1,m=65536,p=4$salt$hash}). The parent implementation
   * splits the entire value by comma to separate the password from roles, which incorrectly
   * truncates the hash at the first comma in the Argon2 parameters.
   * </p>
   * <p>
   * This override wraps {@code $shiro2$} hashed passwords in double quotes before delegating
   * to the parent, leveraging the quote-aware splitting in {@code StringUtils.split()}.
   * </p>
   */
  @Override
  protected void processUserDefinitions(Map<String, String> userDefs) {
    if (userDefs == null || userDefs.isEmpty()) {
      super.processUserDefinitions(userDefs);
      return;
    }
    Map<String, String> fixedDefs = new LinkedHashMap<>();
    for (Map.Entry<String, String> entry : userDefs.entrySet()) {
      fixedDefs.put(entry.getKey(), quoteShiro2CryptPassword(entry.getValue()));
    }
    super.processUserDefinitions(fixedDefs);
  }

  /**
   * Wraps a {@code $shiro2$} hashed password in double quotes to protect internal commas
   * from being interpreted as role delimiters.
   * <p>
   * Rather than assuming a fixed number of {@code $}-delimited sections, this locates the
   * password/roles boundary by searching for a comma only after the last {@code $} in the
   * crypt value. This preserves the current Argon2 behavior while avoiding brittle parsing
   * if the {@code $shiro2$} crypt format changes.
   * </p>
   */
  private String quoteShiro2CryptPassword(String value) {
    if (value == null || !value.startsWith(SHIRO2_PREFIX)) {
      return value;
    }
    int lastDollarIdx = value.lastIndexOf('$');
    if (lastDollarIdx == -1 || lastDollarIdx == value.length() - 1) {
      return value;
    }
    int commaIdx = value.indexOf(',', lastDollarIdx + 1);
    if (commaIdx == -1) {
      // No roles after the hash, no quoting needed
      return value;
    }
    return "\"" + value.substring(0, commaIdx) + "\"" + value.substring(commaIdx);
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    AuthenticationInfo authInfo = super.doGetAuthenticationInfo(token);

    // check for administrator secret, 2FA code validation etc.
    if (authInfo != null && getConfiguration().hasSecretOtp()) {
      String code = token instanceof UsernamePasswordOtpToken ? ((UsernamePasswordOtpToken) token).getOtp() : null;
      if (Strings.isNullOrEmpty(code)) throw new NoSuchOtpException("X-Obiba-" + getConfiguration().getOtpStrategy());
      if (!totpService.validateCode(code, getConfiguration().getSecretOtp()))
        throw new AuthenticationException("Wrong TOTP");
    }

    return authInfo;
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    SimpleAccount account = (SimpleAccount) super.doGetAuthorizationInfo(principals);
    // implicitly, give the role agate-user to all users from ini
    if(account != null) account.addRole(Roles.AGATE_USER.toString());

    return account;
  }

  private Configuration getConfiguration() {
    return configurationService.getConfiguration();
  }
}
