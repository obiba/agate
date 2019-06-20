/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.config;

import com.google.common.collect.Lists;
import com.mongodb.DBObject;
import org.obiba.runtime.Version;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories("org.obiba.agate.repository")
public class MongoDbConfiguration {

  @Bean
  public CustomConversions customConversions() {
    return new CustomConversions(
      Lists.newArrayList(new VersionReadConverter()));
  }

  public static class VersionReadConverter implements Converter<DBObject, Version> {

    @Override
    public Version convert(DBObject dbObject) {
      return new Version((int)dbObject.get("major"), (int)dbObject.get("minor"), (int)dbObject.get("micro"), (String)dbObject.get("qualifier"));
    }
  }

}

