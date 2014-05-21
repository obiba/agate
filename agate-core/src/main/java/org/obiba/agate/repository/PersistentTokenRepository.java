package org.obiba.agate.repository;

import java.util.List;

import org.joda.time.LocalDate;
import org.obiba.agate.domain.PersistentToken;
import org.obiba.agate.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;


/**
 * Spring Data MongoDB repository for the PersistentToken entity.
 */
public interface PersistentTokenRepository extends MongoRepository<PersistentToken, String> {

    List<PersistentToken> findByUser(User user);

    List<PersistentToken> findByTokenDateBefore(LocalDate localDate);

}
