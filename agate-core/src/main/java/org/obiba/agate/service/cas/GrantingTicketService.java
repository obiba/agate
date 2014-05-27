package org.obiba.agate.service.cas;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.GrantingTicket;
import org.obiba.agate.repository.GrantingTicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GrantingTicketService {

  private static final Logger log = LoggerFactory.getLogger(GrantingTicketService.class);

  @Inject
  private GrantingTicketRepository grantingTicketRepository;

  @NotNull
  public GrantingTicket findById(@NotNull String id) throws NoSuchGrantingTicketException {
    String gtId = GrantingTicket.toId(id);
    GrantingTicket ticket = grantingTicketRepository.findOne(gtId);
    if(ticket == null) throw NoSuchGrantingTicketException.withId(id);
    return ticket;
  }

  public List<GrantingTicket> findByUsername(@NotNull String username) {
    return grantingTicketRepository.findByUsername(username);
  }

  public void deleteAll(List<GrantingTicket> tickets) {
    if (tickets == null || tickets.isEmpty()) return;
    for (GrantingTicket ticket : tickets) {
      delete(ticket.getId());
    }
  }

  public void save(@NotNull @Valid GrantingTicket ticket) {
    grantingTicketRepository.save(ticket);
  }

  public void delete(@NotNull String id) {
    String gtId = GrantingTicket.toId(id);
    grantingTicketRepository.delete(gtId);
  }
}
