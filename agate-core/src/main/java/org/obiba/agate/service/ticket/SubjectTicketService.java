package org.obiba.agate.service.ticket;

import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.obiba.agate.domain.SubjectTicket;
import org.obiba.agate.repository.SubjectTicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SubjectTicketService {

  private static final Logger log = LoggerFactory.getLogger(SubjectTicketService.class);

  @Inject
  private SubjectTicketRepository subjectTicketRepository;

  @NotNull
  public SubjectTicket findById(@NotNull String id) throws NoSuchSubjectTicketException {
    SubjectTicket ticket = subjectTicketRepository.findOne(id);
    if(ticket == null) throw NoSuchSubjectTicketException.withId(id);
    return ticket;
  }

  public List<SubjectTicket> findByUsername(@NotNull String username) {
    return subjectTicketRepository.findByUsername(username);
  }

  public void deleteAll(List<SubjectTicket> tickets) {
    if (tickets == null || tickets.isEmpty()) return;
    for (SubjectTicket ticket : tickets) {
      delete(ticket.getId());
    }
  }

  public void save(@NotNull @Valid SubjectTicket ticket) {
    subjectTicketRepository.save(ticket);
  }

  public void delete(@NotNull String id) {
    subjectTicketRepository.delete(id);
  }
}
