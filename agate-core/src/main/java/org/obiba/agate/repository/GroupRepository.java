/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.repository;

import java.util.List;

import org.obiba.agate.domain.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the Group entity.
 */
public interface GroupRepository extends MongoRepository<Group, String> {

  List<Group> findByName(String name);

  List<Group> findByApplications(String application);
}
