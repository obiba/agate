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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.ForbiddenException;

import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.obiba.agate.domain.Authorization;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.domain.User;
import org.obiba.agate.event.AuthorizationDeletedEvent;
import org.obiba.agate.event.UserDeletedEvent;
import org.obiba.agate.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.Subscribe;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

@Service
public class TicketService {

  private static final Logger log = LoggerFactory.getLogger(TicketService.class);

  @Inject
  private TicketRepository ticketRepository;

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private UserService userService;

  @Inject
  private AuthorizationService authorizationService;

  /**
   * Create or reuse a ticket for the given username.
   *
   * @param username
   * @param renew delete any existing tickets (created for the application) of the user before creating a new one
   * @param rememberMe
   * @param application application name issuing the login event
   * @return
   */
  public Ticket create(String username, boolean renew, boolean rememberMe, String application) {
    List<Ticket> tickets = findByUsername(username);
    if(renew) deleteAll(tickets.stream()
        .filter(t -> application.equals(t.getEvents().get(0).getApplication()))
        .collect(Collectors.toList()));

    Ticket ticket = new Ticket();
    ticket.setUsername(username);
    ticket.setRemembered(rememberMe);
    ticket.addEvent(application, "login");
    save(ticket);

    return ticket;
  }

  /**
   * Create or update a ticket by its associated authorization.
   *
   * @param authorization
   * @return
   */
  public Ticket create(Authorization authorization) {
    Optional<Ticket> ticketOptional = ticketRepository.findByAuthorization(authorization.getId()).stream().findFirst();
    Ticket ticket = null;
    if(ticketOptional.isPresent()) {
      ticket = ticketOptional.get();
      DateTime expires = getExpirationDate(ticket);
      if (expires.isBefore(DateTime.now())) {
        ticketRepository.delete(ticket);
        ticket = null;
      }
    }
    if (ticket == null) {
      ticket = new Ticket();
      ticket.setUsername(authorization.getUsername());
      ticket.setRemembered(false);
      ticket.setAuthorization(authorization.getId());
    }
    ticket.addEvent(authorization.getApplication(), "access_token");
    save(ticket);

    return ticket;
  }

  /**
   * Find all {@link Ticket}.
   *
   * @return
   */
  public List<Ticket> findAll() {
    return ticketRepository.findAll();
  }

  /**
   * Find {@link Ticket} by its ID or token.
   *
   * @param idOrToken
   * @return
   * @throws NoSuchTicketException
   */
  @NotNull
  public Ticket getTicket(@NotNull String idOrToken) throws NoSuchTicketException {
    if(isToken(idOrToken)) return getTicketByToken(idOrToken);

    Optional<Ticket> ticket = ticketRepository.findById(idOrToken);
    if(!ticket.isPresent()) throw NoSuchTicketException.withId(idOrToken);
    return ticket.get();
  }

  /**
   * Get the {@link Ticket} corresponding to the given token.
   *
   * @param token
   * @return null if not found
   */
  public Ticket findByToken(@NotNull String token) {
    try {
      Claims claims = Jwts.parser().setSigningKey(configurationService.getConfiguration().getSecretKey().getBytes())
        .parseClaimsJws(token).getBody();
      return ticketRepository.findById(claims.getId()).orElseGet(() -> null);
    } catch(SignatureException e) {
      return null;
    }
  }

  /**
   * Get all {@link Ticket}s for the user name.
   *
   * @param username
   * @return
   */
  public List<Ticket> findByUsername(@NotNull String username) {
    return ticketRepository.findByUsername(username);
  }

  /**
   * Delete the list of {@link Ticket}s.
   *
   * @param tickets
   */
  public void deleteAll(List<Ticket> tickets) {
    if(tickets == null || tickets.isEmpty()) return;
    for(Ticket ticket : tickets) {
      deleteById(ticket.getId());
    }
  }

  /**
   * Delete all {@link Ticket}s of a {@link User}.
   *
   * @param username
   */
  public void deleteAllUserTickets(String username) {
    deleteAll(findByUsername(username));
  }

  /**
   * Insert or update the {@link Ticket}. Set the {@link Ticket}'s token if none.
   *
   * @param ticket
   */
  public void save(@NotNull @Valid Ticket ticket) {
    ticketRepository.save(ticket);
  }

  /**
   * Delete a {@link Ticket} if any is matching the given ID or token.
   *
   * @param idOrToken
   */
  public void delete(@NotNull String idOrToken) {
    if(isToken(idOrToken)) {
      Ticket ticket = findByToken(idOrToken);
      if(ticket != null) deleteById(ticket.getId());
    } else {
      deleteById(idOrToken);
    }
  }

  /**
   * Compute expiration date using configured timeouts.
   *
   * @param ticket
   * @return
   */
  public DateTime getExpirationDate(Ticket ticket) {
    return getExpirationDate(ticket.getCreatedDate(), ticket.isRemembered());
  }

  //
  // Event handling
  //

  @Subscribe
  public void onAuthorizationDeleted(AuthorizationDeletedEvent event) {
    deleteAll(ticketRepository.findByAuthorization(event.getPersistable().getId()));
  }

  @Subscribe
  public void onUserDeleted(UserDeletedEvent event) {
    deleteAll(ticketRepository.findByUsername(event.getPersistable().getName()));
  }

  /**
   * Remembered tickets have to be removed once expired.
   * This is scheduled to get fired everyday, at midnight.
   */
  @Scheduled(cron = "0 0 0 * * *")
  public void removeExpiredRemembered() {
    removeExpired(ticketRepository.findByCreatedDateBeforeAndRemembered(
      DateTime.now().minusHours(configurationService.getConfiguration().getLongTimeout()), true));
  }

  /**
   * Not remembered tickets have to be removed once expired.
   * This is scheduled to get fired every 15 minutes.
   */
  @Scheduled(cron = "0 0/15 * * * *")
  public void removeExpiredNotRemembered() {
    removeExpired(ticketRepository.findByCreatedDateBeforeAndRemembered(
      DateTime.now().minusHours(configurationService.getConfiguration().getShortTimeout()), false));
  }

  //
  // Private methods
  //

  /**
   * Find {@link Ticket} by its token.
   *
   * @param token
   * @return
   * @throws NoSuchTicketException
   */
  private Ticket getTicketByToken(@NotNull String token) {
    Ticket ticket = findByToken(token);
    if(ticket == null) throw NoSuchTicketException.withToken(token);
    return ticket;
  }

  private boolean isToken(String idOrToken) {
    return idOrToken != null && idOrToken.contains(".");
  }

  private DateTime getExpirationDate(DateTime created, boolean remembered) {
    return remembered
      ? created.plusHours(configurationService.getConfiguration().getLongTimeout())
      : created.plusHours(configurationService.getConfiguration().getShortTimeout());
  }

  private void deleteById(@NotNull String id) {
    if(!Strings.isNullOrEmpty(id))
      ticketRepository.deleteById(id);
  }

  private void removeExpired(List<Ticket> tickets) {
    for(Ticket ticket : tickets) {
      // TODO deactivate instead of delete
      ticketRepository.delete(ticket);
    }
  }
}
