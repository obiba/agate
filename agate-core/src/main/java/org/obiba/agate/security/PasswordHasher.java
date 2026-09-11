/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.security;

import com.google.common.base.Strings;
import jakarta.inject.Inject;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Hash and verify the secrets stored by Agate: user passwords and application keys.
 * <p>
 * New hashes are Argon2id with a random salt, stored in Shiro's modular crypt format
 * (<code>$shiro2$argon2id$...</code>). Hashes produced by earlier versions (hex SHA-512 with the global
 * <code>shiro.password.salt</code>) are still verified, so that they can be replaced on the next successful
 * authentication.
 */
@Component
public class PasswordHasher {

  private static final String CRYPT_FORMAT_PREFIX = "$";

  private final DefaultPasswordService passwordService;

  private final String legacySalt;

  private final int legacyIterations;

  @Inject
  public PasswordHasher(Environment env) {
    this(env.getProperty("shiro.password.salt"), env.getProperty("shiro.password.nbHashIterations", Integer.class, 10000));
  }

  public PasswordHasher(String legacySalt, int legacyIterations) {
    this.legacySalt = legacySalt;
    this.legacyIterations = legacyIterations;
    DefaultHashService hashService = new DefaultHashService();
    hashService.setDefaultAlgorithmName(DefaultPasswordService.DEFAULT_HASH_ALGORITHM); // argon2id, provided by shiro-hashes-argon2
    passwordService = new DefaultPasswordService();
    passwordService.setHashService(hashService);
  }

  /**
   * Hash a secret with a fresh random salt.
   *
   * @param plain
   * @return the hash in modular crypt format
   */
  public String hash(String plain) {
    return passwordService.encryptPassword(plain);
  }

  /**
   * Verify a secret against a stored hash, whatever its format.
   *
   * @param plain
   * @param stored
   * @return
   */
  public boolean matches(String plain, String stored) {
    if (plain == null || Strings.isNullOrEmpty(stored)) return false;
    if (isLegacy(stored)) {
      return MessageDigest.isEqual(legacyHash(plain).getBytes(StandardCharsets.UTF_8), stored.getBytes(StandardCharsets.UTF_8));
    }
    try {
      return passwordService.passwordsMatch(plain, stored);
    } catch (RuntimeException e) {
      // unknown or malformed hash format
      return false;
    }
  }

  /**
   * Whether the stored hash was produced by an earlier version and should be replaced once the secret is known.
   *
   * @param stored
   * @return
   */
  public boolean isLegacy(String stored) {
    return !Strings.isNullOrEmpty(stored) && !stored.startsWith(CRYPT_FORMAT_PREFIX);
  }

  private String legacyHash(String plain) {
    return new Sha512Hash(plain, legacySalt, legacyIterations).toString();
  }
}
