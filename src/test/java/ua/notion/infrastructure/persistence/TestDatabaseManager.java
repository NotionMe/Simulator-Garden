package ua.notion.infrastructure.persistence;

import ua.notion.infrastructure.websocket.MockWebSocketApiClient;
import ua.notion.infrastructure.websocket.WebSocketApiClient;

public class TestDatabaseManager {
  private MockWebSocketApiClient apiClient;

  public void setup() throws Exception {
    apiClient = new MockWebSocketApiClient();
  }

  public void teardown() {
    apiClient = null;
  }

  public void cleanDatabase() {
    try {
      setup();
    } catch (Exception e) {
      // Ignore
    }
  }

  public WebSocketApiClient getConnectionPool() {
    return apiClient;
  }

  public PersistenceContext createContext() {
    try {
      setup();
      return new PersistenceContext(apiClient);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create test context", e);
    }
  }

  public void cleanup() {
    teardown();
  }
}
