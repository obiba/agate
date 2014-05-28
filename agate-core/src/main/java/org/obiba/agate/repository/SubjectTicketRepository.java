package org.obiba.agate.repository;

import java.util.List;

import org.obiba.agate.domain.SubjectTicket;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the SubjectTicket entity.
 */
public interface SubjectTicketRepository extends MongoRepository<SubjectTicket, String> {

  List<SubjectTicket> findByUsername(String username);

}
