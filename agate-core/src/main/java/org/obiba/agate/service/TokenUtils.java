/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Nonnull;
import org.joda.time.DateTime;
import org.obiba.agate.domain.Authorization;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.domain.User;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Factory of Json Web Tokens.
 */
@Component
public class TokenUtils {

  public static final String OPENID_SCOPE = "openid";
  public static final String OPENID_EMAIL_SCOPE = "email";
  public static final String OPENID_PROFILE_SCOPE = "profile";
  public static final String OPENID_ADDRESS_SCOPE = "address";
  public static final String OPENID_PHONE_SCOPE = "phone";
  public static final String OPENID_OFFLINE_ACCESS_SCOPE = "offline_access";
  public static final Set<String> OPENID_SCOPES = Sets.newHashSet(
      OPENID_SCOPE,
      OPENID_EMAIL_SCOPE,
      OPENID_PROFILE_SCOPE,
      OPENID_PHONE_SCOPE,
      OPENID_ADDRESS_SCOPE,
      OPENID_OFFLINE_ACCESS_SCOPE); // http://openid.net/specs/openid-connect-core-1_0.html#ScopeClaims
  public static final String OPENID_TOKEN = "id_token";

  /**
   * Scope naming convention is application[:action].
   */
  public static final String SCOPE_DELIMITER = ":";

  @Inject
  private UserService userService;

  @Inject
  private TicketService ticketService;

  @Inject
  private AuthorizationService authorizationService;

  @Inject
  private ConfigurationService configurationService;

  /**
   * Make an access token (json web token) for the ticket.
   *
   * @param ticket
   * @return
   */
  public String makeAccessToken(@Nonnull Ticket ticket) {
    return makeAccessToken(ticket, null);
  }

  public String makeAccessToken(@Nonnull Ticket ticket, String clientId) {
    User user = userService.findUser(ticket.getUsername());

    DateTime expires = ticketService.getExpirationDate(ticket);

    ClaimsBuilder claims = Jwts.claims()
        .subject(ticket.getUsername())
        .issuer(getIssuerID())
        .issuedAt(ticket.getCreatedDate().toDate())
        .expiration(expires.toDate())
        .id(ticket.getId());

    Authorization authorization = null;
    if (ticket.hasAuthorization()) {
      authorization = authorizationService.get(ticket.getAuthorization());
    }

    putAudienceClaim(claims, user, authorization, clientId);
    putContextClaim(claims, user, authorization);

    return Jwts.builder()
        .claims(claims.build())
        .signWith(configurationService.getSecretKeyJWT())
        .compact();
  }

  public String getSignatureAlgorithm() {
    return SignatureAlgorithm.HS256.name();
  }

  /**
   * Validate Json web token: issuer, jwt ID and signature verification.
   *
   * @param token
   * @param application Application name requesting validation
   */
  public void validateAccessToken(@Nonnull String token, @Nonnull String application) {
    try {
      Claims claims = Jwts.parser()
          .verifyWith(configurationService.getSecretKeyJWT())
          .build()
          .parseSignedClaims(token).getPayload();
      if (!getIssuerID().equals(claims.getIssuer())) throw new InvalidTokenException("Token issuer is not valid");
      if (!claims.getAudience().contains(application)) {
        throw new InvalidTokenException("Token is not for '" + application + "'");
      }
    } catch (Exception e) {
      throw new InvalidTokenException("Token signature is not valid");
    }
  }

  /**
   * Make ID token (json web token) for the authorization.
   *
   * @param authorization
   * @param scopes 
   * @return
   */
  public String makeIDToken(@Nonnull Authorization authorization, @Nonnull List<String> scopes) {
    if (!authorization.hasScope(OPENID_SCOPE)) return "";

    DateTime expires = authorizationService.getExpirationDate(authorization);
    ClaimsBuilder claims = buildClaims(authorization.getUsername(), scopes);
    claims.issuedAt(authorization.getCreatedDate().toDate()) //
        .expiration(expires.toDate());
    claims.add(Claims.AUDIENCE, authorization.getApplication());

    return Jwts.builder()
        .claims(claims.build())
        .signWith(configurationService.getSecretKeyJWT())
        .compact();
  }

  public ClaimsBuilder buildClaims(String subject, @Nonnull List<String> scopes) {
    User user = userService.findUser(subject);
    ClaimsBuilder claims = Jwts.claims().subject(subject).issuer(getIssuerID());
    putUserClaims(claims, user, scopes);

    return claims;
  }

  public Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(configurationService.getSecretKeyJWT())
        .build()
        .parseSignedClaims(token).getPayload();
  }

  public String getIssuerID() {
    return configurationService.getPublicUrl();
  }

  //
  // Private methods
  //


  /**
   * The context of the token contains some custom entries about the user and the scope of the authorization (if any).
   *
   * @param claims
   * @param user
   * @param authorization
   */
  private void putContextClaim(ClaimsBuilder claims, User user, Authorization authorization) {
    if (user == null) return;

    Map<String, Object> userMap = Maps.newHashMap();
    String name = "";
    if (user.hasFirstName()) {
      name = user.getFirstName();
      userMap.put("first_name", user.getFirstName());
    }
    if (user.hasLastName()) {
      if (!Strings.isNullOrEmpty(name)) name += " ";
      name += user.getLastName();
      userMap.put("last_name", user.getLastName());
    }
    userMap.put("locale", user.getPreferredLanguage());
    if (!Strings.isNullOrEmpty(name)) userMap.put("name", name);
    userMap.put("groups", user.getGroups());
    Map<String, Object> contextMap = Maps.newHashMap();
    contextMap.put("user", userMap);
    if (authorization != null && authorization.hasScopes()) {
      contextMap.put("scopes", authorization.getScopes());
    }
    claims.add("context", contextMap);
  }

  /**
   * Add the user standard parameters. See http://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
   *
   * @param claims
   * @param user
   */
  private void putUserClaims(ClaimsBuilder claims, User user, List<String> scopes) {
    if (scopes.contains("profile")) {
      putProfileClaims(claims, user);
    }

    if (scopes.contains("email")) {
      putEmailClaims(claims, user);
    }

    if (user.hasGroups())
      claims.add("groups", user.getGroups());
    claims.add("locale", user.getPreferredLanguage());
  }

  private void putEmailClaims(ClaimsBuilder claims, User user) {
    claims.add("email", user.getEmail());
    // TODO
    claims.add("email_verified", false);//user.isEmailVerified());
  }

  private void putProfileClaims(ClaimsBuilder claims, User user) {
    String name = "";

    if (user.hasFirstName()) {
      name = user.getFirstName();
      claims.add("given_name", user.getFirstName());
    }
    if (user.hasLastName()) {
      if (!Strings.isNullOrEmpty(name)) name += " ";
      name += user.getLastName();
      claims.add("family_name", user.getLastName());
    }
    if (!Strings.isNullOrEmpty(name)) claims.add("name", name);
  }

  /**
   * If not bound to an authorization, all applications that can be accessed by the user are the audience of the token,
   * otherwise it is restricted to the one of authorization.
   *
   * @param claims
   * @param user
   * @param authorization
   * @param clientId
   */
  private void putAudienceClaim(ClaimsBuilder claims, User user, Authorization authorization, String clientId) {
    if (user == null) return;

    Set<String> applications = userService.getUserApplications(user);
    if (authorization == null) {
      if (clientId == null) {
        claims.add(Claims.AUDIENCE, applications);
      } else {
        claims.add(Claims.AUDIENCE, Sets.newHashSet(clientId));
      }
    } else {
      Set<String> audience = Sets.newTreeSet();
      // authorized application can always validated the access token
      audience.add(authorization.getApplication());
      if (authorization.hasScopes()) {
        authorization.getScopes().stream().map(this::scopeToApplication).filter(applications::contains)
            .forEach(audience::add);
      }
      claims.add(Claims.AUDIENCE, audience);
    }
  }

  /**
   * Extract the application name from the scope name (syntax is: application[:action]).
   *
   * @param scope
   * @return
   */
  private String scopeToApplication(String scope) {
    if (Strings.isNullOrEmpty(scope)) return scope;
    return scope.split(SCOPE_DELIMITER)[0];
  }

}
