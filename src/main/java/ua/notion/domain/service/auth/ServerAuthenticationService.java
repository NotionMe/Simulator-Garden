package ua.notion.domain.service.auth;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.security.PasswordHasher;
import ua.notion.infrastructure.websocket.WebSocketApiClient;

public class ServerAuthenticationService extends AuthenticationService {
  private final WebSocketApiClient apiClient;

  public ServerAuthenticationService(WebSocketApiClient apiClient) {
    super(null);
    this.apiClient = apiClient;
  }

  @Override
  public User register(String username, String email, String password) {
    validateRegistration(username, email, password);

    String passwordHash = PasswordHasher.hashPassword(password);
    return apiClient.send(
        "create",
        "user",
        Map.of("username", username, "email", email, "password_hash", passwordHash),
        User.class);
  }

  @Override
  public Optional<User> authenticate(String username, String password) {
    if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
      return Optional.empty();
    }

    List<User> users =
        apiClient.send(
            "list",
            "user",
            Map.of(),
            new com.google.gson.reflect.TypeToken<List<User>>() {}.getType());

    if (users == null) {
      return Optional.empty();
    }

    return users.stream()
        .filter(user -> username.trim().equals(user.getUsername()))
        .filter(user -> user.getPasswordHash() != null)
        .filter(user -> PasswordHasher.verifyPassword(password, user.getPasswordHash()))
        .findFirst();
  }

  @Override
  public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
    throw new UnsupportedOperationException(
        "Changing password is not supported by the current server API");
  }

  @Override
  public boolean resetPassword(String email, String newPassword) {
    throw new UnsupportedOperationException(
        "Resetting password is not supported by the current server API");
  }

  private void validateRegistration(String username, String email, String password) {
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
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be empty");
    }
    if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
      throw new IllegalArgumentException("Invalid email format");
    }
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
}
