/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.repository;

import java.util.List;

import org.joda.time.DateTime;
import org.obiba.agate.domain.User;
import org.obiba.agate.domain.UserStatus;
import org.springframework.data.mongodb.repository.MongoRepository;


/**
 * Spring Data MongoDB repository for the User entity.
 */
public interface UserRepository extends MongoRepository<User, String> {

  List<User> findByName(String username);

  List<User> findByNameAndStatus(String username, UserStatus status);

  List<User> findByRole(String role);

  List<User> findByStatus(UserStatus status);

  List<User> findByRoleAndLastLoginLessThan(String role, DateTime dateTime);

  List<User> findByApplications(String application);

  List<User> findByEmail(String email);

  List<User> findByEmailAndStatus(String email, UserStatus active);

  List<User> findByStatusAndGroups(UserStatus status, String group);

  List<User> findByGroups(String group);

  List<User> findByNameAndStatusAndGroups(String username, UserStatus status, String group);
}
