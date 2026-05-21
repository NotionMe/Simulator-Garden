package ua.notion.infrastructure.persistence.impl;

import java.util.List;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.GenericRepository;
import ua.notion.infrastructure.persistence.contract.UserRepository;
import ua.notion.infrastructure.websocket.WebSocketApiClient;

public class UserRepositoryImpl extends GenericRepository<User, Integer> implements UserRepository {

  public UserRepositoryImpl(WebSocketApiClient apiClient) {
    super(apiClient, User.class, "user");
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
    return findByUsername(username).stream()
        .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
  }

  @Override
  public boolean existsByEmail(String email) {
    return findByEmail(email).stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
  }
}
