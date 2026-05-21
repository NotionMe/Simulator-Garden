package ua.notion.infrastructure.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.*;
import java.util.function.Function;
import ua.notion.infrastructure.websocket.WebSocketApiClient;

public abstract class GenericRepository<T, ID> implements Repository<T, ID> {

  protected final WebSocketApiClient apiClient;
  protected final Class<T> entityClass;
  protected final String resourceName;

  protected GenericRepository(
      WebSocketApiClient apiClient, Class<T> entityClass, String resourceName) {
    this.apiClient = apiClient;
    this.entityClass = entityClass;
    this.resourceName = resourceName;
  }

  @Override
  public Optional<T> findById(ID id) {
    try {
      Map<String, Object> payload = Map.of("id", id);
      T entity = apiClient.send("read", resourceName, payload, entityClass);
      return Optional.ofNullable(entity);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Override
  public List<T> findByField(String fieldName, Object value) {
    String camelFieldName = snakeCaseToCamelCase(fieldName);
    return findAll().stream()
        .filter(
            entity -> {
              try {
                Field field = entityClass.getDeclaredField(camelFieldName);
                field.setAccessible(true);
                Object val = field.get(entity);
                if (val == null) return value == null;
                if (val.getClass().isEnum() && value instanceof String) {
                  return ((Enum<?>) val).name().equalsIgnoreCase((String) value);
                }
                if (val instanceof Number && value instanceof Number) {
                  return ((Number) val).doubleValue() == ((Number) value).doubleValue();
                }
                return val.equals(value);
              } catch (Exception e) {
                return false;
              }
            })
        .toList();
  }

  @Override
  public List<T> findAll(
      Filter filter, String sortBy, boolean isAscending, int offset, int limit, String baseSql) {
    return findAll().stream().skip(offset).limit(limit).toList();
  }

  @Override
  public List<T> findAll(Filter filter, String sortBy, boolean isAscending, int offset, int limit) {
    return findAll(filter, sortBy, isAscending, offset, limit, null);
  }

  @Override
  public List<T> findAll(int offset, int limit) {
    return findAll(null, null, true, offset, limit);
  }

  @Override
  public List<T> findAll() {
    Type listType =
        com.google.gson.reflect.TypeToken.getParameterized(List.class, entityClass).getType();
    List<T> result = apiClient.send("list", resourceName, null, listType);
    return result != null ? result : List.of();
  }

  @Override
  public long count(Filter filter) {
    return count();
  }

  @Override
  public long count() {
    return findAll().size();
  }

  @Override
  public <R> List<R> groupBy(Aggregation aggregation, Function<ResultSet, R> resultMapper) {
    throw new UnsupportedOperationException("groupBy operation is not supported via WebSocket");
  }

  @Override
  public T save(T entity) {
    T created = apiClient.send("create", resourceName, entity, entityClass);
    if (created != null) {
      Object id = extractId(created);
      setEntityId(entity, id);
      copyFields(created, entity);
      return entity;
    }
    return created;
  }

  @Override
  public List<T> saveAll(List<T> entities) {
    List<T> saved = new ArrayList<>();
    for (T entity : entities) {
      saved.add(save(entity));
    }
    return saved;
  }

  @Override
  public T update(ID id, T entity) {
    T updated = apiClient.send("update", resourceName, entity, entityClass);
    if (updated != null) {
      copyFields(updated, entity);
    }
    return entity;
  }

  @Override
  public Map<ID, T> updateAll(Map<ID, T> entities) {
    for (Map.Entry<ID, T> entry : entities.entrySet()) {
      update(entry.getKey(), entry.getValue());
    }
    return entities;
  }

  @Override
  public void delete(ID id) {
    Map<String, Object> payload = Map.of("id", id);
    apiClient.send("delete", resourceName, payload, Object.class);
  }

  @Override
  public void deleteAll(List<ID> ids) {
    for (ID id : ids) {
      delete(id);
    }
  }

  public Object extractId(Object entity) {
    try {
      var idField = entity.getClass().getDeclaredField("id");
      idField.setAccessible(true);
      return idField.get(entity);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalStateException(
          "Failed to get identifier for " + entity.getClass().getSimpleName(), e);
    }
  }

  private void setEntityId(T entity, Object id) {
    try {
      var idField = entity.getClass().getDeclaredField("id");
      idField.setAccessible(true);
      if (id instanceof Number && idField.getType() == Integer.class) {
        id = ((Number) id).intValue();
      }
      idField.set(entity, id);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      // Ignore
    }
  }

  private void copyFields(T source, T target) {
    for (Field field : entityClass.getDeclaredFields()) {
      field.setAccessible(true);
      try {
        field.set(target, field.get(source));
      } catch (IllegalAccessException e) {
        // Ignore
      }
    }
  }

  protected static String camelCaseToSnakeCase(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }
    return input.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
  }

  protected static String snakeCaseToCamelCase(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }
    StringBuilder result = new StringBuilder();
    boolean toUpperCase = false;
    for (char ch : input.toCharArray()) {
      if (ch == '_') {
        toUpperCase = true;
      } else {
        result.append(toUpperCase ? Character.toUpperCase(ch) : ch);
        toUpperCase = false;
      }
    }
    return result.toString();
  }
}
