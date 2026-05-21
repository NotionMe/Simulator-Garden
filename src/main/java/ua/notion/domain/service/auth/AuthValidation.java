package ua.notion.domain.service.auth;

final class AuthValidation {

  private AuthValidation() {}

  static void validateRegistration(String username, String email, String password) {
    validateUsername(username);
    validateEmail(email);
    validatePassword(password);
  }

  static void validateLoginInput(String identifier, String password) {
    if (identifier == null || identifier.trim().isEmpty()) {
      throw new IllegalArgumentException("Username or email cannot be empty");
    }
    if (password == null || password.isEmpty()) {
      throw new IllegalArgumentException("Password cannot be empty");
    }
  }

  private static void validateUsername(String username) {
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

  private static void validateEmail(String email) {
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be empty");
    }
    if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
      throw new IllegalArgumentException("Invalid email format");
    }
  }

  private static void validatePassword(String password) {
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
