package io.luxyva.jasony.discuss.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConfig {

  public void test() {
    ConnectionString connectionString = new ConnectionString(
      "mongodb://discuss:ds@xxx/discuss"
    );
    MongoClientSettings settings = MongoClientSettings.builder()
      .applyConnectionString(connectionString)
      .retryWrites(true)
      .build();
    MongoClient mongoClient = MongoClients.create(settings);
    MongoDatabase database = mongoClient.getDatabase("discuss");
    database.listCollectionNames();
  }
}
