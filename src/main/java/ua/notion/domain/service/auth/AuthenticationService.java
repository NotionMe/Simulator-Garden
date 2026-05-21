package ua.notion.domain.service.auth;

import java.time.LocalDateTime;
import java.util.Optional;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.contract.UserRepository;
import ua.notion.infrastructure.security.PasswordHasher;

public class AuthenticationService {
  private final PersistenceContext context;
  private final UserRepository userRepository;

  public AuthenticationService(PersistenceContext context) {
    this.context = context;
    this.userRepository = context.getUserRepository();
  }

  public User register(String username, String email, String password) {
    validateUsername(username);
    validateEmail(email);
    validatePassword(password);

    if (isUsernameTaken(username)) {
      throw new IllegalArgumentException("Username already taken");
    }

    if (isEmailTaken(email)) {
      throw new IllegalArgumentException("Email already registered");
    }

    String passwordHash = PasswordHasher.hashPassword(password);

    User user =
        User.builder()
            .username(username)
            .email(email)
            .passwordHash(passwordHash)
            .createdAt(LocalDateTime.now())
            .build();

    try {
      return userRepository.save(user);
    } catch (Exception e) {
      throw new RuntimeException("Failed to register user", e);
    }
  }

  public Optional<User> authenticate(String username, String password) {
    Optional<User> userOpt =
        userRepository.findAll().stream().filter(u -> u.getUsername().equals(username)).findFirst();

    if (userOpt.isEmpty()) {
      return Optional.empty();
    }

    User user = userOpt.get();
    if (user.getPasswordHash() != null
        && !PasswordHasher.verifyPassword(password, user.getPasswordHash())) {
      return Optional.empty();
    }

    return Optional.of(user);
  }

  public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
    Optional<User> userOpt = userRepository.findById(userId);
    if (userOpt.isEmpty()) {
      return false;
    }

    User user = userOpt.get();
    if (user.getPasswordHash() != null
        && !PasswordHasher.verifyPassword(oldPassword, user.getPasswordHash())) {
      return false;
    }

    validatePassword(newPassword);

    String newPasswordHash = PasswordHasher.hashPassword(newPassword);
    user.setPasswordHash(newPasswordHash);

    try {
      userRepository.update(user.getId(), user);
      return true;
    } catch (Exception e) {
      throw new RuntimeException("Failed to change password", e);
    }
  }

  public boolean resetPassword(String email, String newPassword) {
    Optional<User> userOpt =
        userRepository.findAll().stream().filter(u -> u.getEmail().equals(email)).findFirst();

    if (userOpt.isEmpty()) {
      return false;
    }

    validatePassword(newPassword);

    User user = userOpt.get();
    String newPasswordHash = PasswordHasher.hashPassword(newPassword);
    user.setPasswordHash(newPasswordHash);

    try {
      userRepository.update(user.getId(), user);
      return true;
    } catch (Exception e) {
      throw new RuntimeException("Failed to reset password", e);
    }
  }

  private void validateUsername(String username) {
    if (username == null || username.trim().isEmpty()) {
      throw new IllegalArgumentException("Username cannot be empty");
    }
    if (username.length() < 3) {
      throw new IllegalArgumentException("Username must be at least 3 characters");
    }
    if (username.length() > 50) {
      throw new IllegalArgumentException("Username must be at most 50 characters");
    }
    if (!username.matches("^[a-zA-Z0-9_]+$")) {
      throw new IllegalArgumentException(
          "Username can only contain letters, numbers, and underscores");
    }
  }

  private void validateEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be empty");
    }
    if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
      throw new IllegalArgumentException("Invalid email format");
    }
  }

  private void validatePassword(String password) {
    if (password == null || password.isEmpty()) {
      throw new IllegalArgumentException("Password cannot be empty");
    }
    if (password.length() < 8) {
      throw new IllegalArgumentException("Password must be at least 8 characters");
    }
    if (password.length() > 128) {
      throw new IllegalArgumentException("Password must be at most 128 characters");
    }
  }

  private boolean isUsernameTaken(String username) {
    return userRepository.findAll().stream().anyMatch(u -> u.getUsername().equals(username));
  }

  private boolean isEmailTaken(String email) {
    return userRepository.findAll().stream().anyMatch(u -> u.getEmail().equals(email));
  }
}
