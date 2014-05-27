package org.obiba.agate.service.cas;

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
import org.obiba.agate.domain.GrantingTicket;
import org.obiba.agate.repository.GrantingTicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
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
@ContextConfiguration(classes = GrantingTicketServiceTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GrantingTicketServiceTest {

  private static final Logger log = LoggerFactory.getLogger(GrantingTicketServiceTest.class);

  @Inject
  private GrantingTicketService ticketService;

  @Inject
  private GrantingTicketRepository grantingTicketRepository;

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

    GrantingTicket ticket = new GrantingTicket();
    ticket.setUsername("pwel");
    ticketService.save(ticket);

    List<GrantingTicket> tickets = grantingTicketRepository.findAll();
    log.info(">>> tickets: {}", tickets);
    assertThat(tickets).hasSize(1);

    GrantingTicket ticketSaved = tickets.get(0);
    assertThat(ticket.getId()).isNotEmpty();
    assertThat(ticket.getCASId().startsWith("TGT")).isTrue();
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

    @Bean
    public PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
      return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public GrantingTicketService studyService() {
      return new GrantingTicketService();
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
