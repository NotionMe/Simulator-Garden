package ua.notion.infrastructure.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseRepositoryTest {
  protected TestDatabaseManager dbManager;
  protected PersistenceContext context;

  @BeforeEach
  void setUp() throws Exception {
    dbManager = new TestDatabaseManager();
    dbManager.setup();
    context = new PersistenceContext(dbManager.getConnectionPool());
  }

  @AfterEach
  void tearDown() throws Exception {
    if (context != null) {
      context.close();
    }
    if (dbManager != null) {
      dbManager.teardown();
    }
  }
}
