package ua.notion.domain.service.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.TestDatabaseManager;

class AuthenticationServiceTest {
  private TestDatabaseManager testDbManager;
  private PersistenceContext context;
  private AuthenticationService authService;

  @BeforeEach
  void setUp() throws Exception {
    testDbManager = new TestDatabaseManager();
    testDbManager.setup();
    context = new PersistenceContext(testDbManager.getConnectionPool());
    authService = new AuthenticationService(context);
  }

  @AfterEach
  void tearDown() {
    if (context != null) {
      context.close();
    }
    testDbManager.teardown();
  }

  @Test
  void testRegisterUser() {
    User user = authService.register("testuser", "test@example.com", "password123");

    assertNotNull(user);
    assertNotNull(user.getId());
    assertEquals("testuser", user.getUsername());
    assertEquals("test@example.com", user.getEmail());
    assertNotNull(user.getPasswordHash());
    assertNotEquals("password123", user.getPasswordHash());
    assertNotNull(user.getCreatedAt());
  }

  @Test
  void testRegisterUserWithShortUsername() {
    assertThrows(
        IllegalArgumentException.class,
        () -> authService.register("ab", "test@example.com", "password123"));
  }

  @Test
  void testRegisterUserWithInvalidEmail() {
    assertThrows(
        IllegalArgumentException.class,
        () -> authService.register("testuser", "invalid-email", "password123"));
  }

  @Test
  void testRegisterUserWithShortPassword() {
    assertThrows(
        IllegalArgumentException.class,
        () -> authService.register("testuser", "test@example.com", "short"));
  }

  @Test
  void testRegisterUserWithDuplicateUsername() {
    authService.register("testuser", "test1@example.com", "password123");

    assertThrows(
        IllegalArgumentException.class,
        () -> authService.register("testuser", "test2@example.com", "password456"));
  }

  @Test
  void testRegisterUserWithDuplicateEmail() {
    authService.register("testuser1", "test@example.com", "password123");

    assertThrows(
        IllegalArgumentException.class,
        () -> authService.register("testuser2", "test@example.com", "password456"));
  }

  @Test
  void testAuthenticateSuccess() {
    authService.register("testuser", "test@example.com", "password123");

    Optional<User> authenticated = authService.authenticate("testuser", "password123");

    assertTrue(authenticated.isPresent());
    assertEquals("testuser", authenticated.get().getUsername());
  }

  @Test
  void testAuthenticateWrongPassword() {
    authService.register("testuser", "test@example.com", "password123");

    Optional<User> authenticated = authService.authenticate("testuser", "wrongpassword");

    assertFalse(authenticated.isPresent());
  }

  @Test
  void testAuthenticateNonExistentUser() {
    Optional<User> authenticated = authService.authenticate("nonexistent", "password123");

    assertFalse(authenticated.isPresent());
  }

  @Test
  void testChangePassword() {
    User user = authService.register("testuser", "test@example.com", "oldpassword");

    boolean changed = authService.changePassword(user.getId(), "oldpassword", "newpassword123");

    assertTrue(changed);

    Optional<User> authenticated = authService.authenticate("testuser", "newpassword123");
    assertTrue(authenticated.isPresent());

    Optional<User> oldAuth = authService.authenticate("testuser", "oldpassword");
    assertFalse(oldAuth.isPresent());
  }

  @Test
  void testChangePasswordWrongOldPassword() {
    User user = authService.register("testuser", "test@example.com", "oldpassword");

    boolean changed = authService.changePassword(user.getId(), "wrongpassword", "newpassword123");

    assertFalse(changed);
  }

  @Test
  void testChangePasswordInvalidNewPassword() {
    User user = authService.register("testuser", "test@example.com", "oldpassword");

    assertThrows(
        IllegalArgumentException.class,
        () -> authService.changePassword(user.getId(), "oldpassword", "short"));
  }

  @Test
  void testResetPassword() {
    authService.register("testuser", "test@example.com", "oldpassword");

    boolean reset = authService.resetPassword("test@example.com", "newpassword123");

    assertTrue(reset);

    Optional<User> authenticated = authService.authenticate("testuser", "newpassword123");
    assertTrue(authenticated.isPresent());
  }

  @Test
  void testResetPasswordNonExistentEmail() {
    boolean reset = authService.resetPassword("nonexistent@example.com", "newpassword123");

    assertFalse(reset);
  }

  @Test
  void testPasswordHashingIsDifferentEachTime() {
    User user1 = authService.register("user1", "user1@example.com", "password123");
    User user2 = authService.register("user2", "user2@example.com", "password123");

    assertNotEquals(user1.getPasswordHash(), user2.getPasswordHash());
  }
}
