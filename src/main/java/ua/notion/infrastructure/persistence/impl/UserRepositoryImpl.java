package ua.notion.infrastructure.persistence.impl;

import java.util.List;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.GenericRepository;
import ua.notion.infrastructure.persistence.contract.UserRepository;
import ua.notion.infrastructure.persistence.util.ConnectionPool;

public class UserRepositoryImpl extends GenericRepository<User, Integer> implements UserRepository {

  public UserRepositoryImpl(ConnectionPool connectionPool) {
    super(connectionPool, User.class, "users");
  }

  @Override
  public List<User> findByUsername(String username) {
    return findByField("username", username);
  }

  @Override
  public List<User> findByEmail(String email) {
    return findByField("email", email);
  }

  @Override
  public boolean existsByUsername(String username) {
    Filter filter =
        (whereClause, params) -> {
          whereClause.add("username = ?");
          params.add(username);
        };
    return count(filter) > 0;
  }

  @Override
  public boolean existsByEmail(String email) {
    Filter filter =
        (whereClause, params) -> {
          whereClause.add("email = ?");
          params.add(email);
        };
    return count(filter) > 0;
  }
}
