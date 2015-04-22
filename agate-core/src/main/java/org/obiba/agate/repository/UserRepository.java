package org.obiba.agate.repository;

import java.util.List;

import org.obiba.agate.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;


/**
 * Spring Data MongoDB repository for the User entity.
 */
public interface UserRepository extends MongoRepository<User, String> {

  List<User> findByName(String username);

  List<User> findByRole(String role);
}
