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
import org.joda.time.DateTime;
import org.obiba.runtime.Version;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Date;

@Configuration
@EnableMongoRepositories("org.obiba.agate.repository")
public class MongoDbConfiguration {

  @Bean
  public MongoCustomConversions customConversions() {
    return new MongoCustomConversions(
      Lists.newArrayList(
          new DateConverter(),
          new VersionReadConverter()));
  }

  public static class DateConverter implements Converter<Date, DateTime> {

    @Override
    public DateTime convert(Date source) {
      return new DateTime(source);
    }
  }

  public static class VersionReadConverter implements Converter<DBObject, Version> {

    @Override
    public Version convert(DBObject dbObject) {
      return new Version((int)dbObject.get("major"), (int)dbObject.get("minor"), (int)dbObject.get("micro"), (String)dbObject.get("qualifier"));
    }
  }

}

