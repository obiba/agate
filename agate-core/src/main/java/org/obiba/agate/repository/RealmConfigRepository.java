package org.obiba.agate.repository;

import org.obiba.agate.domain.RealmConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RealmConfigRepository extends MongoRepository<RealmConfig, String> {
  RealmConfig findOneByName(String name);
  List<RealmConfig> findByType(String realm);
  List<RealmConfig> findByForSignupTrue();
}
