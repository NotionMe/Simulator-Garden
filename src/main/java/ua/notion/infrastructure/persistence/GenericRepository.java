package ua.notion.infrastructure.persistence;

import java.lang.reflect.Field;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import ua.notion.infrastructure.persistence.exception.DatabaseAccessException;
import ua.notion.infrastructure.persistence.exception.EntityMappingException;
import ua.notion.infrastructure.persistence.util.ConnectionPool;

public abstract class GenericRepository<T, ID> implements Repository<T, ID> {

  protected final ConnectionPool connectionPool;
  protected final Class<T> entityClass;
  protected final String tableName;
  private UnitOfWork unitOfWork;

  protected GenericRepository(
      ConnectionPool connectionPool, Class<T> entityClass, String tableName) {
    this.connectionPool = connectionPool;
    this.entityClass = entityClass;
    this.tableName = tableName;
  }

  public void setUnitOfWork(UnitOfWork unitOfWork) {
    this.unitOfWork = unitOfWork;
  }

  protected Connection getConnection() throws SQLException {
    if (unitOfWork != null && unitOfWork.isActive()) {
      return unitOfWork.getConnection();
    }
    return connectionPool.getConnection();
  }

  protected boolean shouldCloseConnection() {
    return unitOfWork == null || !unitOfWork.isActive();
  }

  @Override
  public Optional<T> findById(ID id) {
    return findByField("id", id).stream().findFirst();
  }

  @Override
  public List<T> findByField(String fieldName, Object value) {
    String sql = String.format("SELECT * FROM %s WHERE %s = ?", tableName, fieldName);
    return executeQuery(sql, stmt -> stmt.setObject(1, value));
  }

  @Override
  public List<T> findAll(
      Filter filter, String sortBy, boolean isAscending, int offset, int limit, String baseSql) {
    StringJoiner sql = new StringJoiner(" ");
    sql.add(baseSql);
    List<Object> parameters = new ArrayList<>();

    if (filter != null) {
      StringJoiner whereClause = new StringJoiner(" AND ", " WHERE ", "");
      filter.apply(whereClause, parameters);
      sql.add(whereClause.toString());
    }
    if (sortBy != null && !sortBy.isEmpty()) {
      sql.add("ORDER BY " + sortBy + (isAscending ? " ASC" : " DESC"));
    }
    sql.add("LIMIT ? OFFSET ?");
    parameters.add(limit);
    parameters.add(offset);

    return executeQuery(sql.toString(), stmt -> setParameters(stmt, parameters));
  }

  @Override
  public List<T> findAll(Filter filter, String sortBy, boolean isAscending, int offset, int limit) {
    return findAll(
        filter, sortBy, isAscending, offset, limit, String.format("SELECT * FROM %s", tableName));
  }

  @Override
  public List<T> findAll() {
    String sql = String.format("SELECT * FROM %s", tableName);
    return executeQuery(sql, stmt -> {});
  }

  @Override
  public List<T> findAll(int offset, int limit) {
    String sql = String.format("SELECT * FROM %s LIMIT ? OFFSET ?", tableName);
    return executeQuery(
        sql,
        stmt -> {
          stmt.setInt(1, limit);
          stmt.setInt(2, offset);
        });
  }

  @Override
  public long count(Filter filter) {
    return count(filter, tableName);
  }

  protected long count(Filter filter, String tableName) {
    StringJoiner sql = new StringJoiner(" ");
    sql.add(String.format("SELECT COUNT(*) FROM %s", tableName));
    List<Object> parameters = new ArrayList<>();

    if (filter != null) {
      StringJoiner whereClause = new StringJoiner(" AND ", " WHERE ", "");
      filter.apply(whereClause, parameters);
      sql.add(whereClause.toString());
    }

    try (Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql.toString())) {
      setParameters(statement, parameters);
      try (ResultSet resultSet = statement.executeQuery()) {
        return resultSet.next() ? resultSet.getLong(1) : 0;
      }
    } catch (SQLException e) {
      throw new DatabaseAccessException("Помилка підрахунку записів у таблиці " + tableName, e);
    }
  }

  @Override
  public long count() {
    String sql = String.format("SELECT COUNT(*) FROM %s", tableName);
    Connection connection = null;
    boolean shouldClose = false;

    try {
      connection = getConnection();
      shouldClose = shouldCloseConnection();

      PreparedStatement statement = connection.prepareStatement(sql);
      ResultSet resultSet = statement.executeQuery();

      long count = resultSet.next() ? resultSet.getLong(1) : 0;

      resultSet.close();
      statement.close();
      return count;
    } catch (SQLException e) {
      throw new DatabaseAccessException("Помилка підрахунку записів", e);
    } finally {
      if (shouldClose && connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          // Ігнор
        }
      }
    }
  }

  @Override
  public <R> List<R> groupBy(Aggregation aggregation, Function<ResultSet, R> resultMapper) {
    StringJoiner selectClause = new StringJoiner(", ", "SELECT ", "");
    StringJoiner groupByClause = new StringJoiner(", ", " GROUP BY ", "");
    aggregation.apply(selectClause, groupByClause);
    String sql = String.format("%s FROM %s%s", selectClause, tableName, groupByClause);

    try (Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {
      List<R> results = new ArrayList<>();
      while (resultSet.next()) {
        results.add(resultMapper.apply(resultSet));
      }
      return results;
    } catch (SQLException e) {
      throw new DatabaseAccessException("Помилка групування сутностей", e);
    }
  }

  @Override
  public T save(T entity) {
    String sql = buildInsertSql(entity);
    List<Object> values = extractEntityValues(entity, false);

    Connection connection = null;
    boolean shouldCloseConnection = false;

    try {
      connection = getConnection();
      shouldCloseConnection = (unitOfWork == null || !unitOfWork.isActive());

      PreparedStatement statement =
          connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      setParameters(statement, values);
      statement.executeUpdate();

      try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          Object id = generatedKeys.getObject(1);
          setEntityId(entity, id);
        }
      }
      statement.close();
    } catch (SQLException e) {
      throw new DatabaseAccessException("Помилка збереження сутності: " + sql, e);
    } finally {
      if (shouldCloseConnection && connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          // Ігнор
        }
      }
    }

    return entity;
  }

  @Override
  public List<T> saveAll(List<T> entities) {
    if (entities.isEmpty()) {
      return entities;
    }

    String sql = buildInsertSql(entities.get(0));
    try (Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      for (T entity : entities) {
        List<Object> values = extractEntityValues(entity);
        setParameters(statement, values);
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new DatabaseAccessException("Помилка пакетного збереження сутностей", e);
    }

    return entities;
  }

  @Override
  public T update(ID id, T entity) {
    String sql = buildUpdateSql();
    List<Object> values = extractEntityValues(entity, false);
    values.add(id);
    executeUpdate(sql, values);
    return entity;
  }

  @Override
  public Map<ID, T> updateAll(Map<ID, T> entities) {
    if (entities.isEmpty()) {
      return entities;
    }

    String sql = buildUpdateSql();
    try (Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      for (Map.Entry<ID, T> entry : entities.entrySet()) {
        List<Object> values = extractEntityValues(entry.getValue());
        values.add(entry.getKey());
        setParameters(statement, values);
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new DatabaseAccessException("Помилка пакетного оновлення сутностей", e);
    }

    return entities;
  }

  @Override
  public void delete(ID id) {
    String sql = String.format("DELETE FROM %s WHERE id = ?", tableName);
    executeUpdate(sql, List.of(id));
  }

  @Override
  public void deleteAll(List<ID> ids) {
    if (ids.isEmpty()) {
      return;
    }

    String sql = String.format("DELETE FROM %s WHERE id = ?", tableName);
    try (Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      for (ID id : ids) {
        statement.setObject(1, id);
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      throw new DatabaseAccessException("Помилка пакетного видалення сутностей", e);
    }
  }

  protected List<T> executeQuery(String sql, ParameterSetter parameterSetter) {
    try (Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      parameterSetter.setParameters(statement);
      try (ResultSet resultSet = statement.executeQuery()) {
        List<T> entities = new ArrayList<>();
        while (resultSet.next()) {
          entities.add(mapResultSetToEntity(resultSet));
        }
        return entities;
      }
    } catch (SQLException e) {
      throw new DatabaseAccessException("Помилка виконання запиту: " + sql, e);
    }
  }

  protected <R> List<R> executeQuery(
      String sql, ParameterSetter parameterSetter, RowMapper<R> mapper) {
    try (Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      parameterSetter.setParameters(statement);
      try (ResultSet resultSet = statement.executeQuery()) {
        List<R> results = new ArrayList<>();
        while (resultSet.next()) {
          results.add(mapper.map(resultSet));
        }
        return results;
      }
    } catch (SQLException e) {
      throw new DatabaseAccessException("Помилка виконання запиту: " + sql, e);
    }
  }

  protected void executeUpdate(String sql, List<Object> parameters) {
    Connection connection = null;
    PreparedStatement statement = null;
    boolean shouldClose = shouldCloseConnection();

    try {
      connection = getConnection();
      statement = connection.prepareStatement(sql);
      setParameters(statement, parameters);
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseAccessException("Помилка виконання оновлення: " + sql, e);
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException e) {
          // Ignore
        }
      }
      if (shouldClose && connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          // Ignore
        }
      }
    }
  }

  protected void setParameters(PreparedStatement statement, List<Object> parameters)
      throws SQLException {
    for (int i = 0; i < parameters.size(); i++) {
      statement.setObject(i + 1, parameters.get(i));
    }
  }

  protected String buildInsertSql(T entity) {
    StringJoiner columns = new StringJoiner(", ");
    StringJoiner placeholders = new StringJoiner(", ");
    for (Field field : entityClass.getDeclaredFields()) {
      if (field.getName().equals("id")) continue;
      columns.add(camelCaseToSnakeCase(field.getName()));
      placeholders.add("?");
    }
    return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
  }

  protected String buildUpdateSql() {
    StringJoiner setClause = new StringJoiner(", ");
    for (Field field : entityClass.getDeclaredFields()) {
      if (field.getName().equals("id")) continue;
      setClause.add(camelCaseToSnakeCase(field.getName()) + " = ?");
    }
    return String.format("UPDATE %s SET %s WHERE id = ?", tableName, setClause);
  }

  protected List<Object> extractEntityValues(T entity, boolean includeId) {
    List<Object> values = new ArrayList<>();
    for (Field field : entityClass.getDeclaredFields()) {
      if (!includeId && field.getName().equals("id")) {
        continue;
      }
      field.setAccessible(true);
      try {
        Object value = field.get(entity);
        if (value != null) {
          if (field.getType().isEnum()) {
            value = ((Enum<?>) value).name();
          } else if (field.getType() == LocalDateTime.class) {
            // SQLite: зберігаємо як текст у форматі "YYYY-MM-DD HH:MM:SS"
            value =
                ((LocalDateTime) value)
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
          } else if (field.getType() == LocalDate.class) {
            value = Date.valueOf((LocalDate) value);
          }
        }
        values.add(value);
      } catch (IllegalAccessException e) {
        throw new EntityMappingException("Помилка доступу до поля: " + field.getName(), e);
      }
    }
    return values;
  }

  protected List<Object> extractEntityValues(T entity) {
    return extractEntityValues(entity, true);
  }

  protected T mapResultSetToEntity(ResultSet rs) throws SQLException {
    try {
      T entity = entityClass.getDeclaredConstructor().newInstance();
      for (Field field : entityClass.getDeclaredFields()) {
        field.setAccessible(true);
        String columnName = camelCaseToSnakeCase(field.getName());
        Object value = rs.getObject(columnName);
        if (value != null) {
          field.set(entity, convertValue(value, field.getType()));
        }
      }
      return entity;
    } catch (Exception e) {
      throw new EntityMappingException("Помилка зіставлення ResultSet із сутністю", e);
    }
  }

  protected Object convertValue(Object value, Class<?> targetType) {
    if (value == null && !targetType.isPrimitive()) {
      return null;
    }
    return switch (targetType.getName()) {
      case "java.lang.String" -> value.toString();
      case "java.lang.Integer", "int" ->
          value instanceof Number
              ? ((Number) value).intValue()
              : Integer.parseInt(value.toString());
      case "java.lang.Boolean", "boolean" ->
          value instanceof Number
              ? ((Number) value).intValue() != 0
              : Boolean.parseBoolean(value.toString());
      case "java.time.LocalDateTime" -> {
        if (value instanceof Timestamp) {
          yield ((Timestamp) value).toLocalDateTime();
        } else if (value instanceof String) {
          yield LocalDateTime.parse(
              value.toString().replace(" ", "T")); // "2026-05-10 18:00:00" -> ISO format
        } else if (value instanceof Number) {
          yield LocalDateTime.ofInstant(
              java.time.Instant.ofEpochMilli(((Number) value).longValue()),
              java.time.ZoneId.systemDefault());
        }
        yield null;
      }
      case "java.time.LocalDate" -> value instanceof Date ? ((Date) value).toLocalDate() : null;
      default -> value;
    };
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

  public Object extractId(Object entity) {
    try {
      var idField = entity.getClass().getDeclaredField("id");
      idField.setAccessible(true);
      return idField.get(entity);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalStateException(
          "Не вдалося отримати ідентифікатор для " + entity.getClass().getSimpleName(), e);
    }
  }

  private void setEntityId(T entity, Object id) {
    try {
      var idField = entity.getClass().getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(entity, id);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new EntityMappingException("Помилка встановлення ID сутності", e);
    }
  }

  @FunctionalInterface
  protected interface ParameterSetter {
    void setParameters(PreparedStatement statement) throws SQLException;
  }
}
