/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.obiba.agate.domain.Authorization;
import org.obiba.agate.domain.Configuration;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.domain.User;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

/**
 * Factory of Json Web Tokens.
 */
@Component
public class TokenUtils {

  public static final String OPENID_SCOPE = "openid";

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
  public String makeAccessToken(@NotNull Ticket ticket) {
    User user = userService.findUser(ticket.getUsername());

    DateTime expires = ticketService.getExpirationDate(ticket);

    Claims claims = Jwts.claims().setSubject(ticket.getUsername()) //
      .setIssuer(getIssuerID()) //
      .setIssuedAt(ticket.getCreatedDate().toDate()) //
      .setExpiration(expires.toDate()) //
      .setId(ticket.getId());

    Authorization authorization = null;
    if(ticket.hasAuthorization()) {
      authorization = authorizationService.get(ticket.getAuthorization());
    }

    putAudienceClaim(claims, user, authorization);
    putContextClaim(claims, user, authorization);

    return Jwts.builder().setClaims(claims)
      .signWith(SignatureAlgorithm.HS256, configurationService.getConfiguration().getSecretKey().getBytes()).compact();
  }

  /**
   * Validate Json web token: issuer, jwt ID and signature verification.
   *
   * @param token
   * @param application Application name requesting validation
   */
  public void validateAccessToken(@NotNull String token, @NotNull String application) {
    try {
      Claims claims = Jwts.parser().setSigningKey(configurationService.getConfiguration().getSecretKey().getBytes())
        .parseClaimsJws(token).getBody();
      if(!getIssuerID().equals(claims.getIssuer())) throw new InvalidTokenException("Token issuer is not valid");
      if(!claims.getAudience().contains(application)) {
        throw new InvalidTokenException("Token is not for '" + application + "'");
      }
    } catch(SignatureException e) {
      throw new InvalidTokenException("Token signature is not valid");
    }
  }

  /**
   * Make ID token (json web token) for the authorization.
   *
   * @param authorization
   * @return
   */
  public String makeIDToken(@NotNull Authorization authorization) {
    if(!authorization.hasScope(OPENID_SCOPE)) return "";

    User user = userService.findUser(authorization.getUsername());

    DateTime expires = authorizationService.getExpirationDate(authorization);

    Claims claims = Jwts.claims().setSubject(authorization.getUsername()) //
      .setIssuer(getIssuerID()) //
      .setIssuedAt(authorization.getCreatedDate().toDate()) //
      .setExpiration(expires.toDate());

    claims.put(Claims.AUDIENCE, authorization.getApplication());
    putUserClaims(claims, user);

    return Jwts.builder().setClaims(claims)
      .signWith(SignatureAlgorithm.HS256, configurationService.getConfiguration().getSecretKey().getBytes()).compact();
  }

  //
  // Private methods
  //

  private String getIssuerID() {
    Configuration configuration = configurationService.getConfiguration();
    return configuration.hasPublicUrl() ? configurationService.getPublicUrl() : "agate:" + configuration.getId();
  }

  /**
   * The context of the token contains some custom entries about the user and the scope of the authorization (if any).
   *
   * @param claims
   * @param user
   * @param authorization
   * @param ticket
   */
  private void putContextClaim(Claims claims, User user, Authorization authorization) {
    if(user == null) return;

    Map<String, Object> userMap = Maps.newHashMap();
    String name = "";
    if(user.hasFirstName()) {
      name = user.getFirstName();
      userMap.put("first_name", user.getFirstName());
    }
    if(user.hasLastName()) {
      if(!Strings.isNullOrEmpty(name)) name += " ";
      name += user.getLastName();
      userMap.put("last_name", user.getLastName());
    }
    if(!Strings.isNullOrEmpty(name)) userMap.put("name", name);
    userMap.put("groups", user.getGroups());
    Map<String, Object> contextMap = Maps.newHashMap();
    contextMap.put("user", userMap);
    if(authorization != null && authorization.hasScopes()) {
      contextMap.put("scopes", authorization.getScopes());
    }
    claims.put("context", contextMap);
  }

  /**
   * Add the user standard parameters. See http://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
   *
   * @param claims
   * @param user
   */
  private void putUserClaims(Claims claims, User user) {
    String name = "";
    if(user.hasFirstName()) {
      name = user.getFirstName();
      claims.put("given_name", user.getFirstName());
    }
    if(user.hasLastName()) {
      if(!Strings.isNullOrEmpty(name)) name += " ";
      name += user.getLastName();
      claims.put("family_name", user.getLastName());
    }
    if(!Strings.isNullOrEmpty(name)) claims.put("name", name);

    claims.put("email", user.getEmail());
    // TODO
    claims.put("email_verified", false);//user.isEmailVerified());
    //claims.put("locale", user.getLocale());
  }

  /**
   * If not bound to an authorization, all applications that can be accessed by the user are the audience of the token,
   * otherwise it is restricted to the one of authorization.
   *
   * @param claims
   * @param user
   * @param authorization
   */
  private void putAudienceClaim(Claims claims, User user, Authorization authorization) {
    if(user == null) return;

    Set<String> applications = userService.getUserApplications(user);
    if(authorization == null) {
      claims.put(Claims.AUDIENCE, applications);
    } else {
      Set<String> audience = Sets.newTreeSet();
      // authorized application can always validated the access token
      audience.add(authorization.getApplication());
      if(authorization.hasScopes()) {
        authorization.getScopes().stream().map(this::scopeToApplication).filter(applications::contains)
          .forEach(audience::add);
      }
      claims.put(Claims.AUDIENCE, audience);
    }
  }

  /**
   * Extract the application name from the scope name (syntax is: application[:action]).
   *
   * @param scope
   * @return
   */
  private String scopeToApplication(String scope) {
    if(Strings.isNullOrEmpty(scope)) return scope;
    return scope.split(SCOPE_DELIMITER)[0];
  }

}
