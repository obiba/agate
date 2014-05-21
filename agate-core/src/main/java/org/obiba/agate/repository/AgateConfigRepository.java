package org.obiba.agate.repository;

import org.obiba.agate.domain.AgateConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AgateConfigRepository extends MongoRepository<AgateConfig, String> {

}
