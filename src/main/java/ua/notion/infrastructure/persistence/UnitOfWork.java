package ua.notion.infrastructure.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import ua.notion.infrastructure.persistence.exception.DatabaseAccessException;
import ua.notion.infrastructure.persistence.util.ConnectionPool;

public class UnitOfWork implements AutoCloseable {
  private final ConnectionPool connectionPool;
  private Connection connection;
  private boolean isActive = false;

  // Use IdentityHashMap to track entities by reference, not by hashCode
  private final Set<Object> newEntities = Collections.newSetFromMap(new IdentityHashMap<>());
  private final Set<Object> dirtyEntities = Collections.newSetFromMap(new IdentityHashMap<>());
  private final Set<Object> removedEntities = Collections.newSetFromMap(new IdentityHashMap<>());

  public UnitOfWork(ConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
  }

  public void begin() {
    try {
      if (isActive) {
        throw new IllegalStateException("Transaction already active");
      }
      connection = connectionPool.getConnection();
      connection.setAutoCommit(false);
      isActive = true;
    } catch (SQLException e) {
      throw new DatabaseAccessException("Failed to begin transaction", e);
    }
  }

  public void commit() {
    try {
      if (!isActive) {
        throw new IllegalStateException("No active transaction");
      }
      connection.commit();
      clear();
    } catch (SQLException e) {
      rollback();
      throw new DatabaseAccessException("Failed to commit transaction", e);
    } finally {
      isActive = false;
    }
  }

  public void rollback() {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.rollback();
      }
      clear();
    } catch (SQLException e) {
      throw new DatabaseAccessException("Failed to rollback transaction", e);
    } finally {
      isActive = false;
    }
  }

  public void registerNew(Object entity) {
    if (removedEntities.contains(entity)) {
      throw new IllegalStateException("Cannot register removed entity as new");
    }
    if (!dirtyEntities.contains(entity)) {
      newEntities.add(entity);
    }
  }

  public void registerDirty(Object entity) {
    if (!newEntities.contains(entity) && !removedEntities.contains(entity)) {
      dirtyEntities.add(entity);
    }
  }

  public void registerRemoved(Object entity) {
    if (newEntities.contains(entity)) {
      newEntities.remove(entity);
      return;
    }
    dirtyEntities.remove(entity);
    if (!removedEntities.contains(entity)) {
      removedEntities.add(entity);
    }
  }

  public Connection getConnection() {
    if (!isActive) {
      throw new IllegalStateException("No active transaction");
    }
    return connection;
  }

  public boolean isActive() {
    return isActive;
  }

  public Set<Object> getNewEntities() {
    return Collections.unmodifiableSet(newEntities);
  }

  public Set<Object> getDirtyEntities() {
    return Collections.unmodifiableSet(dirtyEntities);
  }

  public Set<Object> getRemovedEntities() {
    return Collections.unmodifiableSet(removedEntities);
  }

  private void clear() {
    newEntities.clear();
    dirtyEntities.clear();
    removedEntities.clear();
  }

  @Override
  public void close() {
    if (isActive) {
      rollback();
    }
  }
}
