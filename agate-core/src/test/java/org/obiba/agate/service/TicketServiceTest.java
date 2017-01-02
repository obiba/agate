/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.agate.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obiba.agate.domain.Ticket;
import org.obiba.agate.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.google.common.io.Files;
import com.mongodb.Mongo;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

import static org.obiba.agate.assertj.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(DependencyInjectionTestExecutionListener.class)
@ContextConfiguration(classes = TicketServiceTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TicketServiceTest {

  private static final Logger log = LoggerFactory.getLogger(TicketServiceTest.class);

  @Inject
  private TicketRepository ticketRepository;

  @Inject
  private MongoTemplate mongoTemplate;

  @BeforeClass
  public static void init() {
    SecurityUtils.setSecurityManager(new DefaultSecurityManager());
  }

  @Before
  public void clearDatabase() {
    mongoTemplate.getDb().dropDatabase();
  }

  @Test
  public void test_create_and_load_new_ticket() throws Exception {

    Ticket ticket = new Ticket();
    ticket.setUsername("pwel");
    ticketRepository.save(ticket);

    List<Ticket> tickets = ticketRepository.findAll();
    log.info(">>> tickets: {}", tickets);
    assertThat(tickets).hasSize(1);

    Ticket ticketSaved = tickets.get(0);
    assertThat(ticket.getId()).isNotEmpty();
    assertThat(ticket.isRemembered()).isFalse();
    assertThat(ticketSaved.getUsername()).isEqualTo(ticket.getUsername());
  }

  @Configuration
  @EnableMongoRepositories("org.obiba.agate.repository")
  static class Config extends AbstractMongoConfiguration {

    static final File BASE_REPO = Files.createTempDir();

    static {
      BASE_REPO.deleteOnExit();
    }

    @Override
    protected String getDatabaseName() {
      return "agate-test";
    }

    @Override
    public Mongo mongo() throws IOException {
      return MongodForTestsFactory.with(Version.Main.PRODUCTION).newMongo();
    }

    @Override
    protected String getMappingBasePackage() {
      return "org.obiba.agate.domain";
    }

  }

}
