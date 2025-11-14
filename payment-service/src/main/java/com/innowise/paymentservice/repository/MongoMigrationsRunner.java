package com.innowise.paymentservice.repository;

import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.ext.mongodb.database.MongoLiquibaseDatabase;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@NullMarked
public class MongoMigrationsRunner implements CommandLineRunner {

  @Value("${spring.mongodb.uri}")
  private String mongoUri;

  @Override
  public void run(String... args) throws Exception {
    var mongoLiquibaseDatabase = (MongoLiquibaseDatabase) DatabaseFactory.getInstance()
        .openDatabase(mongoUri, null, null, null, null);
    try (Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.yaml",
        new ClassLoaderResourceAccessor(), mongoLiquibaseDatabase)) {
      liquibase.update();
    }
  }
}
