package ua.notion;

import java.time.LocalDateTime;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.UnitOfWork;
import ua.notion.infrastructure.persistence.util.ConnectionPool;
import ua.notion.infrastructure.persistence.util.DatabaseInitializer;

public class Main {
  public static void main(String[] args) {
    String databaseUrl = "jdbc:sqlite:./data/garden.db";

    DatabaseInitializer.ensureDatabase(databaseUrl);

    ConnectionPool.PoolConfig config =
        new ConnectionPool.PoolConfig.Builder().withUrl(databaseUrl).build();

    ConnectionPool connectionPool = new ConnectionPool(config);
    PersistenceContext persistenceContext = new PersistenceContext(connectionPool);

    try {
      // Begin transaction using Unit of Work
      UnitOfWork unitOfWork = persistenceContext.beginTransaction();

      try {
        User newUser =
            User.builder()
                .username("testuser_" + System.currentTimeMillis())
                .email("test_" + System.currentTimeMillis() + "@example.com")
                .createdAt(LocalDateTime.now())
                .build();

        // Register entity as new
        unitOfWork.registerNew(newUser);

        // Save user
        persistenceContext.getUserRepository().save(newUser);
        System.out.println("User saved: " + newUser);

        // Query all users
        var users = persistenceContext.getUserRepository().findAll();
        System.out.println("Total users: " + users.size());

        // Commit transaction
        persistenceContext.commitTransaction();
        System.out.println("Transaction committed successfully");

      } catch (Exception e) {
        // Rollback on error
        persistenceContext.rollbackTransaction();
        System.err.println("Transaction rolled back due to error");
        e.printStackTrace();
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      persistenceContext.close();
    }
  }
}
