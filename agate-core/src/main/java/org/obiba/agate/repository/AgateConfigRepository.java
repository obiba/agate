package org.obiba.agate.repository;

import org.obiba.agate.domain.Configuration;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AgateConfigRepository extends MongoRepository<Configuration, String> {

}
