package org.obiba.agate.repository;

import org.obiba.agate.domain.RealmConfig;
import org.obiba.agate.domain.RealmStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RealmConfigRepository extends MongoRepository<RealmConfig, String> {
  RealmConfig findOneByName(String name);
  List<RealmConfig> findAllByStatus(RealmStatus status);
  List<RealmConfig> findAllByStatusAndForSignupTrue(RealmStatus status);
  List<RealmConfig> findByType(String realm);
  List<RealmConfig> findByForSignupTrue();
  RealmConfig findOneByDefaultRealmTrue();
}
