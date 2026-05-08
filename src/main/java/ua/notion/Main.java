package ua.notion;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.time.LocalDateTime;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.config.PersistenceModule;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.UnitOfWork;

public class Main {
  public static void main(String[] args) {
    // Create Guice injector with persistence module
    Injector injector = Guice.createInjector(new PersistenceModule());

    // Get PersistenceContext from injector
    PersistenceContext persistenceContext = injector.getInstance(PersistenceContext.class);

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
