package org.obiba.agate.repository;

import java.util.List;

import org.obiba.agate.domain.GrantingTicket;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the GrantingTicket entity.
 */
public interface GrantingTicketRepository extends MongoRepository<GrantingTicket, String> {

  List<GrantingTicket> findByUsername(String username);

}
