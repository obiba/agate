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

import org.obiba.agate.domain.UserCredentials;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the {@link org.obiba.agate.domain.UserCredentials} entity.
 */
public interface UserCredentialsRepository extends MongoRepository<UserCredentials, String> {

  List<UserCredentials> findByName(String username);

  UserCredentials findOneByName(String username);
}
