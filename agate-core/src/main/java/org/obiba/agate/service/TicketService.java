package org.obiba.agate.service;

import java.util.List;
import java.util.UUID;

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

@Service
public class TicketService {

  private static final Logger log = LoggerFactory.getLogger(TicketService.class);

  @Inject
  private TicketRepository ticketRepository;

  @Inject
  private ConfigurationService configurationService;

  /**
   * Find all {@link org.obiba.agate.domain.Ticket}.
   *
   * @return
   */
  public List<Ticket> findAll() {
    return ticketRepository.findAll();
  }

  /**
   * Find {@link org.obiba.agate.domain.Ticket} by its token.
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
   * Get the {@link org.obiba.agate.domain.Ticket} corresponding to the given token.
   *
   * @param token
   * @return null if not found
   */
  public Ticket findByToken(@NotNull String token) {
    List<Ticket> tickets = ticketRepository.findByToken(token);
    return tickets != null && !tickets.isEmpty() ? tickets.iterator().next() : null;
  }

  /**
   * Get all {@link org.obiba.agate.domain.Ticket}s for the user name.
   *
   * @param username
   * @return
   */
  public List<Ticket> findByUsername(@NotNull String username) {
    return ticketRepository.findByUsername(username);
  }

  /**
   * Delete the list of {@link org.obiba.agate.domain.Ticket}s.
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
   * Insert or update the {@link org.obiba.agate.domain.Ticket}. Set the {@link org.obiba.agate.domain.Ticket}'s token if none.
   *
   * @param ticket
   */
  public void save(@NotNull @Valid Ticket ticket) {
    if(!ticket.hasToken()) {
      UUID token = UUID.randomUUID();
      Ticket found = findByToken(token.toString());
      while(found != null) {
        token = UUID.randomUUID();
        found = findByToken(token.toString());
      }
      ticket.setToken(token.toString());
    }
    ticketRepository.save(ticket);
  }

  /**
   * Delete a {@link org.obiba.agate.domain.Ticket} if any is matching the given token.
   *
   * @param token
   */
  public void delete(@NotNull String token) {
    Ticket ticket = findByToken(token);
    if(ticket != null) deleteById(ticket.getId());
  }

  private void deleteById(@NotNull String id) {
    if(!StringUtils.isNullOrEmpty(id)) ticketRepository.delete(id);
  }

  /**
   * Remembered tickets have to be removed once expired.
   * This is scheduled to get fired everyday, at midnight.
   */
  @Scheduled(cron = "0 0 0 * * ?")
  public void removeExpiredRemembered() {
    removeExpired(ticketRepository.findByCreatedDateBeforeAndRemembered(
        DateTime.now().minusHours(configurationService.getConfiguration().getLongTimeout() * 3600), true));
  }

  /**
   * Not remembered tickets have to be removed once expired.
   * This is scheduled to get fired every hour.
   */
  @Scheduled(cron = "0 * 0 * * ?")
  public void removeExpiredNotRemembered() {
    removeExpired(ticketRepository.findByCreatedDateBeforeAndRemembered(
        DateTime.now().minusHours(configurationService.getConfiguration().getShortTimeout() * 3600), false));
  }

  private void removeExpired(List<Ticket> tickets) {
    for(Ticket ticket : tickets) {
      // TODO deactivate instead of delete
      ticketRepository.delete(ticket);
    }
  }
}
