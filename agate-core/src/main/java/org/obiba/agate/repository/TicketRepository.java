/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
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
import org.obiba.agate.domain.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the SubjectTicket entity.
 */
public interface TicketRepository extends MongoRepository<Ticket, String> {

  List<Ticket> findByAuthorization(String authorization);

  List<Ticket> findByUsername(String username);

  List<Ticket> findByCreatedDateBeforeAndRemembered(DateTime localDate, boolean remembered);

}
