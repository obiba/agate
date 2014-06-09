package org.obiba.agate.repository;

import java.util.List;

import org.joda.time.DateTime;
import org.obiba.agate.domain.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the SubjectTicket entity.
 */
public interface TicketRepository extends MongoRepository<Ticket, String> {

  List<Ticket> findByToken(String token);

  List<Ticket> findByUsername(String username);

  List<Ticket> findByCreatedDateBeforeAndRemembered(DateTime localDate, boolean remembered);

}
