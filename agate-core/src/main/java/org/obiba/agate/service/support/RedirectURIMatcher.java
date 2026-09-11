/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service.support;

import com.google.common.base.Strings;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * Compare a redirect URI submitted by a client against a registered one, component by component. A plain string
 * prefix test is not enough: the registered value <code>https://app.example.org</code> is a prefix of
 * <code>https://app.example.org.attacker.test/</code>.
 */
public final class RedirectURIMatcher {

  private RedirectURIMatcher() {
  }

  /**
   * Check whether the candidate is covered by at least one of the registered URIs.
   *
   * @see #matches(String, String)
   */
  public static boolean matchesAny(Collection<String> registered, String candidate) {
    return registered != null && registered.stream().anyMatch(r -> matches(r, candidate));
  }

  /**
   * The candidate matches when scheme, host and port are the same as the registered URI's, and its path is the
   * registered path or a sub-path of it (whole path segments only). A query string is allowed, a fragment or
   * user info is not.
   *
   * @param registered URI as configured on the application
   * @param candidate  URI received in a request
   * @return true if the candidate is an acceptable redirect target
   */
  public static boolean matches(String registered, String candidate) {
    if (Strings.isNullOrEmpty(registered) || Strings.isNullOrEmpty(candidate)) return false;
    URI reg;
    URI cand;
    try {
      reg = new URI(registered.trim());
      cand = new URI(candidate.trim());
    } catch (URISyntaxException e) {
      return false;
    }
    if (!reg.isAbsolute() || !cand.isAbsolute() || reg.isOpaque() || cand.isOpaque()) return false;
    if (cand.getRawFragment() != null || cand.getRawUserInfo() != null) return false;
    if (reg.getHost() == null || cand.getHost() == null) return false;

    if (!reg.getScheme().equalsIgnoreCase(cand.getScheme())) return false;
    if (!reg.getHost().equalsIgnoreCase(cand.getHost())) return false;
    if (port(reg) != port(cand)) return false;

    return isSamePathOrSubPath(reg.getRawPath(), cand.getRawPath());
  }

  private static int port(URI uri) {
    if (uri.getPort() != -1) return uri.getPort();
    return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : "http".equalsIgnoreCase(uri.getScheme()) ? 80 : -1;
  }

  private static boolean isSamePathOrSubPath(String registeredPath, String candidatePath) {
    String base = normalizePath(registeredPath);
    String path = normalizePath(candidatePath);
    if (base.equals(path)) return true;
    // registered path is a directory: any descendant is acceptable
    String prefix = base.endsWith("/") ? base : base + "/";
    return path.startsWith(prefix);
  }

  /**
   * Resolve dot segments (a candidate is not allowed to climb out of the registered path) and ignore a trailing
   * slash, so that <code>/callback</code> and <code>/callback/</code> are the same location.
   */
  private static String normalizePath(String path) {
    if (Strings.isNullOrEmpty(path)) return "/";
    String normalized = URI.create(path).normalize().getRawPath();
    if (!normalized.startsWith("/")) normalized = "/" + normalized;
    while (normalized.length() > 1 && normalized.endsWith("/")) normalized = normalized.substring(0, normalized.length() - 1);
    return normalized;
  }
}
