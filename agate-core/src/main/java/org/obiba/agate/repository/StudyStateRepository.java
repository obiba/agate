package org.obiba.agate.repository;

import java.util.List;

import org.obiba.agate.domain.StudyState;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the StudyState entity.
 */
public interface StudyStateRepository extends MongoRepository<StudyState, String> {

  List<StudyState> findByPublishedTagNotNull();

}
