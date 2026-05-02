package ua.notion.infrastructure.persistence;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;

public interface Repository<T, ID> {

  @FunctionalInterface
  interface Filter {
    void apply(StringJoiner whereClause, List<Object> parameters);
  }

  @FunctionalInterface
  interface Aggregation {
    void apply(StringJoiner selectClause, StringJoiner groupByClause);
  }

  @FunctionalInterface
  interface RowMapper<R> {
    R map(ResultSet rs);
  }

  Optional<T> findById(ID id);

  List<T> findByField(String fieldName, Object value);

  List<T> findAll(
      Filter filter, String sortBy, boolean isAscending, int offset, int limit, String baseSql);

  List<T> findAll(Filter filter, String sortBy, boolean isAscending, int offset, int limit);

  List<T> findAll(int offset, int limit);

  List<T> findAll();

  long count(Filter filter);

  long count();

  <R> List<R> groupBy(Aggregation aggregation, Function<ResultSet, R> resultMapper);

  T save(T entity);

  List<T> saveAll(List<T> entities);

  T update(ID id, T entity);

  Map<ID, T> updateAll(Map<ID, T> entities);

  void delete(ID id);

  void deleteAll(List<ID> ids);

  Object extractId(Object entity);
}
