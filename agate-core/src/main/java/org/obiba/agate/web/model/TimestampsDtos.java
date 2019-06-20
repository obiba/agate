/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.web.model;

import org.obiba.mongodb.domain.Timestamped;

class TimestampsDtos {

  private TimestampsDtos() {}

  static Agate.TimestampsDto asDto(Timestamped timestamped) {
    Agate.TimestampsDto.Builder builder = Agate.TimestampsDto.newBuilder()
        .setCreated(timestamped.getCreatedDate().toString());
    if(timestamped.getLastModifiedDate() != null) builder.setLastUpdate(timestamped.getLastModifiedDate().toString());
    return builder.build();
  }

}
