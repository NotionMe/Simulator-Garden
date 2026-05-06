package ua.notion.infrastructure.persistence.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionPool {
  private final String url;
  private Connection connection;

  public ConnectionPool(PoolConfig config) {
    this.url = config.url;
  }

  public Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
      connection = DriverManager.getConnection(url);
      connection.createStatement().execute("PRAGMA foreign_keys = ON");
    }
    return connection;
  }

  public void shutdown() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Помилка закриття з'єднання", e);
    }
  }

  public static class PoolConfig {
    private static final String DEFAULT_URL = "jdbc:sqlite:./data/garden.db";

    private final String url;

    private PoolConfig(Builder builder) {
      this.url = builder.url;
    }

    public static PoolConfig fromProperties(Properties properties) {
      return new Builder().withUrl(properties.getProperty("db.url", DEFAULT_URL)).build();
    }

    public static class Builder {
      private String url = DEFAULT_URL;

      public Builder withUrl(String url) {
        this.url = url;
        return this;
      }

      public PoolConfig build() {
        return new PoolConfig(this);
      }
    }
  }
}
