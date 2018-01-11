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
import org.obiba.agate.domain.Authorization;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the {@link Authorization} entity.
 */
public interface AuthorizationRepository extends MongoRepository<Authorization, String> {

  List<Authorization> findByCode(String code);

  List<Authorization> findByUsername(String username);

  List<Authorization> findByApplication(String application);

  List<Authorization> findByUsernameAndApplication(String username, String application);

  List<Authorization> findByCreatedDateBefore(DateTime localDate);

}
