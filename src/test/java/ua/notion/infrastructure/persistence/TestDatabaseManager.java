package ua.notion.infrastructure.persistence;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;
import ua.notion.infrastructure.persistence.util.ConnectionPool;

public class TestDatabaseManager {
  private ConnectionPool connectionPool;
  private Path tempDbFile;

  public void setup() throws Exception {
    tempDbFile = Files.createTempFile("test-db-", ".db");
    String dbUrl = "jdbc:sqlite:" + tempDbFile.toAbsolutePath();

    ConnectionPool.PoolConfig config =
        new ConnectionPool.PoolConfig.Builder().withUrl(dbUrl).build();
    connectionPool = new ConnectionPool(config);

    Connection conn = connectionPool.getConnection();

    conn.createStatement().execute("PRAGMA foreign_keys = ON");

    // Apply all migrations
    applyMigration(conn, "db/migration/V1__Create_schema.sql");
    applyMigration(conn, "db/migration/V3__Add_password_to_users.sql");
  }

  private void applyMigration(Connection conn, String migrationPath) throws Exception {
    String migrationSql =
        new BufferedReader(
                new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream(migrationPath)))
            .lines()
            .collect(Collectors.joining("\n"));

    Statement stmt = conn.createStatement();
    StringBuilder currentStatement = new StringBuilder();

    for (String line : migrationSql.split("\n")) {
      String trimmed = line.trim();

      if (trimmed.isEmpty() || trimmed.startsWith("--")) {
        continue;
      }

      currentStatement.append(line).append("\n");

      if (trimmed.endsWith(";")) {
        String sql = currentStatement.toString().trim();
        if (!sql.isEmpty()) {
          stmt.execute(sql);
        }
        currentStatement = new StringBuilder();
      }
    }

    stmt.close();
  }

  public void teardown() {
    if (connectionPool != null) {
      connectionPool.shutdown();
    }
    try {
      if (tempDbFile != null) {
        Files.deleteIfExists(tempDbFile);
      }
    } catch (Exception e) {
      // Ігнор
    }
  }

  public void cleanDatabase() throws SQLException {
    Connection conn = connectionPool.getConnection();
    conn.createStatement().execute("PRAGMA foreign_keys = OFF");
    conn.createStatement().execute("DELETE FROM tasks");
    conn.createStatement().execute("DELETE FROM plant_instances");
    conn.createStatement().execute("DELETE FROM weather_events");
    conn.createStatement().execute("DELETE FROM achievements");
    conn.createStatement().execute("DELETE FROM garden_plants");
    conn.createStatement().execute("DELETE FROM gardens");
    conn.createStatement().execute("DELETE FROM plants");
    conn.createStatement().execute("DELETE FROM users");
    conn.createStatement().execute("PRAGMA foreign_keys = ON");
  }

  public ConnectionPool getConnectionPool() {
    return connectionPool;
  }
}
