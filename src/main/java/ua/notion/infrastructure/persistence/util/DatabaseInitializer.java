package ua.notion.infrastructure.persistence.util;

import org.flywaydb.core.Flyway;

public class DatabaseInitializer {
  private final String databaseUrl;

  public DatabaseInitializer(String databaseUrl) {
    this.databaseUrl = databaseUrl;
  }

  public void initialize() {
    System.out.println("Initializing database with Flyway...");

    Flyway flyway =
        Flyway.configure()
            .dataSource(databaseUrl, null, null)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load();

    int migrationsApplied = flyway.migrate().migrationsExecuted;

    if (migrationsApplied > 0) {
      System.out.println("Applied " + migrationsApplied + " migration(s)");
    } else {
      System.out.println("Database is up to date");
    }
  }

  public static void ensureDatabase(String databaseUrl) {
    new DatabaseInitializer(databaseUrl).initialize();
  }
}
