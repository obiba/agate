package org.obiba.agate.upgrade;

import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.UpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class Agate282Upgrade implements UpgradeStep {

  private static final Logger logger = LoggerFactory.getLogger(Agate282Upgrade.class);

  private final MongoTemplate mongoTemplate;

  public Agate282Upgrade(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public String getDescription() {
    return "Upgrade data to 2.8.2";
  }

  @Override
  public Version getAppliesTo() {
    return new Version(2, 8, 2);
  }

  @Override
  public void execute(Version version) {
    logger.info("Executing Agate upgrade to version 2.8.2");
    try {
      MongoCursor<Document> users = mongoTemplate.getCollection("user").find().cursor();
      while (users.hasNext()) {
        Document user = users.next();
        String name = user.getString("name");
        String email = user.getString("email");
        if (name.contains(" ") || email.contains(" ")) {
          user.put("name", name.trim());
          user.put("email", email.trim());
          mongoTemplate.save(user, "user");
        }
      }
    } catch (Exception e) {
      logger.error("Failed to clean up user name and emails", e);
    }
  }
}
