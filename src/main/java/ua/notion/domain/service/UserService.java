package ua.notion.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.contract.UserRepository;

public class UserService {
  private final PersistenceContext context;
  private final UserRepository userRepository;

  public UserService(PersistenceContext context) {
    this.context = context;
    this.userRepository = context.getUserRepository();
  }

  public User createUser(String username, String email) {
    User user =
        User.builder().username(username).email(email).createdAt(LocalDateTime.now()).build();

    context.beginTransaction();
    try {
      userRepository.save(user);
      context.commitTransaction();
      return user;
    } catch (Exception e) {
      context.rollbackTransaction();
      throw new RuntimeException("Failed to create user", e);
    }
  }

  public Optional<User> findUserById(Integer id) {
    return userRepository.findById(id);
  }

  public Optional<User> findUserByUsername(String username) {
    return userRepository.findAll().stream()
        .filter(u -> u.getUsername().equals(username))
        .findFirst();
  }

  public Optional<User> findUserByEmail(String email) {
    return userRepository.findAll().stream().filter(u -> u.getEmail().equals(email)).findFirst();
  }

  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  public void updateUser(User user) {
    if (user.getId() == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }

    context.beginTransaction();
    try {
      userRepository.update(user.getId(), user);
      context.commitTransaction();
    } catch (Exception e) {
      context.rollbackTransaction();
      throw new RuntimeException("Failed to update user", e);
    }
  }

  public void deleteUser(Integer id) {
    context.beginTransaction();
    try {
      userRepository.delete(id);
      context.commitTransaction();
    } catch (Exception e) {
      context.rollbackTransaction();
      throw new RuntimeException("Failed to delete user", e);
    }
  }
}
