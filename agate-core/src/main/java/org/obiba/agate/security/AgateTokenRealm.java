/*******************************************************************************
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.agate.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserCredentials;
import org.obiba.agate.service.TicketService;
import org.obiba.agate.service.TokenUtils;
import org.obiba.agate.service.UserService;
import org.obiba.shiro.authc.TicketAuthenticationToken;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;

/**
 * Realm for users defined in Agate's database accessing using an OAuth2 token (ticket).
 */
@Component
public class AgateTokenRealm extends AuthorizingRealm {
  public static final String AGATE_TOKEN_REALM = "agate-token-realm";

  @Inject
  private UserService userService;

  @Inject
  private TicketService ticketService;

  @Inject
  private TokenUtils tokenUtils;

  @PostConstruct
  public void postConstruct() {
    setCacheManager(new MemoryConstrainedCacheManager());
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof TicketAuthenticationToken;
  }

  @Override
  public String getName() {
    return AGATE_TOKEN_REALM;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    TicketAuthenticationToken ticketAuthenticationToken = (TicketAuthenticationToken) token;
    String ticketId = ticketAuthenticationToken.getTicketId();
    Ticket ticket = ticketService.getTicket(ticketAuthenticationToken.getTicketId());
    ticket.addEvent("agate", "validate");
    ticketService.save(ticket);
    String username = ticket.getUsername();

    // Null username is invalid
    if(username == null) {
      throw new AccountException("Null usernames are not allowed by this realm.");
    }

    User user = userService.findActiveUser(username);

    if(user == null) {
      user = userService.findActiveUserByEmail(username);
      username = user.getName();
    }

    if(user == null || !user.isEnabled() || !user.getRealm().equals(AgateUserRealm.AGATE_REALM)) {
      throw new UnknownAccountException("No account found for user [" + username + "]");
    }

    UserCredentials userCredentials = userService.findUserCredentials(username);
    if(userCredentials == null) throw new UnknownAccountException("No account found for user [" + username + "]");

    List<String> principals = Lists.newArrayList(username);
    if(!Strings.isNullOrEmpty(ticketId)) principals.add(ticketId);
    return new SimpleAuthenticationInfo(new SimplePrincipalCollection(principals, getName()), token.getCredentials());
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    Collection<?> thisPrincipals = principals.fromRealm(getName());

    if(thisPrincipals != null && !thisPrincipals.isEmpty()) {
      Optional<List<String>> scopes = thisPrincipals.stream().map(p -> {
        try {
          return getScopesFromToken(p.toString());
        } catch(MalformedJwtException e) {
          //ignore
          return null;
        }
      }).filter(s -> s != null).findFirst();

      if (scopes.isPresent()) return new SimpleAuthorizationInfo(Sets.newHashSet(scopes.get()));
    }

    return new SimpleAuthorizationInfo();
  }

  private List<String> getScopesFromToken(String token) {
    Claims claims = tokenUtils.parseClaims(token);

    return (List<String>)claims.get("context", Map.class).getOrDefault("scopes", Lists.newArrayList());
  }
}
