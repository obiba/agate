package org.obiba.agate.service;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mysql.jdbc.StringUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class TicketService {

  private static final Logger log = LoggerFactory.getLogger(TicketService.class);

  @Inject
  private TicketRepository ticketRepository;

  @Inject
  private ConfigurationService configurationService;

  /**
   * Create or reuse a ticket for the given username.
   *
   * @param username
   * @param renew delete any existing tickets for the username before creating a new one
   * @param rememberMe
   * @param application application name issuing the login event
   * @return
   */
  public Ticket createTicket(String username, boolean renew, boolean rememberMe, String application) {
    Ticket ticket;
    List<Ticket> tickets = findByUsername(username);
    if(renew) deleteAll(tickets);
    if(renew || tickets == null || tickets.isEmpty()) {
      ticket = new Ticket();
      ticket.setUsername(username);
    } else {
      ticket = tickets.get(0);
    }
    ticket.setRemembered(rememberMe);
    ticket.addEvent(application, "login");
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
   * Find {@link Ticket} by its token.
   *
   * @param token
   * @return
   * @throws NoSuchTicketException
   */
  @NotNull
  public Ticket getTicket(@NotNull String token) throws NoSuchTicketException {
    Ticket ticket = findByToken(token);
    if(ticket == null) throw NoSuchTicketException.withToken(token);
    return ticket;
  }

  /**
   * Get the {@link Ticket} corresponding to the given token.
   *
   * @param token
   * @return null if not found
   */
  public Ticket findByToken(@NotNull String token) {
    List<Ticket> tickets = ticketRepository.findByToken(token);
    return tickets != null && !tickets.isEmpty() ? tickets.iterator().next() : null;
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
    if(!ticket.hasToken()) {
      String token = newToken(ticket.getUsername());
      Ticket found = findByToken(token);
      while(found != null) {
        token = newToken(ticket.getUsername());
        found = findByToken(token);
      }
      ticket.setToken(token);
    }
    ticketRepository.save(ticket);
  }

  /**
   * Delete a {@link Ticket} if any is matching the given token.
   *
   * @param token
   */
  public void delete(@NotNull String token) {
    Ticket ticket = findByToken(token);
    if(ticket != null) deleteById(ticket.getId());
  }

  //
  // Event handling
  //

  /**
   * Remembered tickets have to be removed once expired.
   * This is scheduled to get fired everyday, at midnight.
   */
  @Scheduled(cron = "0 0 0 * * ?")
  public void removeExpiredRemembered() {
    removeExpired(ticketRepository.findByCreatedDateBeforeAndRemembered(
      DateTime.now().minusHours(configurationService.getConfiguration().getLongTimeout()), true));
  }

  /**
   * Not remembered tickets have to be removed once expired.
   * This is scheduled to get fired every hour.
   */
  @Scheduled(cron = "0 * 0 * * ?")
  public void removeExpiredNotRemembered() {
    removeExpired(ticketRepository.findByCreatedDateBeforeAndRemembered(
      DateTime.now().minusHours(configurationService.getConfiguration().getShortTimeout()), false));
  }

  //
  // Private methods
  //

  /**
   * Make a json web token.
   *
   * @param username
   * @return
   */
  private String newToken(@NotNull String username) {
    return Jwts.builder().setSubject(username)
      .signWith(SignatureAlgorithm.HS512, configurationService.getConfiguration().getSecretKey().getBytes()).compact();
  }

  private void deleteById(@NotNull String id) {
    if(!StringUtils.isNullOrEmpty(id)) ticketRepository.delete(id);
  }

  private void removeExpired(List<Ticket> tickets) {
    for(Ticket ticket : tickets) {
      // TODO deactivate instead of delete
      ticketRepository.delete(ticket);
    }
  }
}
