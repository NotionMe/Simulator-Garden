package ua.notion.domain.service.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.websocket.MockWebSocketApiClient;
import ua.notion.infrastructure.websocket.WebSocketApiException;

class WebSocketAuthenticationServiceTest {

  private WebSocketAuthenticationService authService;

  @BeforeEach
  void setUp() {
    authService = new WebSocketAuthenticationService(new MockWebSocketApiClient());
  }

  @Test
  void buildLoginPayloadUsesUsername() {
    Map<String, String> payload =
        WebSocketAuthenticationService.buildLoginPayload("john_doe", "secret123");

    assertEquals("john_doe", payload.get("username"));
    assertEquals("secret123", payload.get("password"));
    assertNull(payload.get("email"));
  }

  @Test
  void buildLoginPayloadUsesEmail() {
    Map<String, String> payload =
        WebSocketAuthenticationService.buildLoginPayload("john@example.com", "secret123");

    assertEquals("john@example.com", payload.get("email"));
    assertEquals("secret123", payload.get("password"));
    assertNull(payload.get("username"));
  }

  @Test
  void registerAndLoginWithUsername() throws WebSocketApiException {
    User registered = authService.register("testuser", "test@example.com", "password123");

    assertNotNull(registered.getId());
    assertEquals("testuser", registered.getUsername());
    assertNull(registered.getPasswordHash());

    Optional<User> loggedIn = authService.login("testuser", "password123");

    assertTrue(loggedIn.isPresent());
    assertEquals(registered.getId(), loggedIn.get().getId());
  }

  @Test
  void loginWithEmail() throws WebSocketApiException {
    authService.register("testuser", "test@example.com", "password123");

    Optional<User> loggedIn = authService.login("test@example.com", "password123");

    assertTrue(loggedIn.isPresent());
    assertEquals("testuser", loggedIn.get().getUsername());
  }

  @Test
  void loginWrongPassword() {
    authService.register("testuser", "test@example.com", "password123");

    WebSocketApiException ex =
        assertThrows(
            WebSocketApiException.class, () -> authService.login("testuser", "wrongpassword"));

    assertEquals("Invalid credentials", ex.getMessage());
  }

  @Test
  void loginUnknownUser() {
    WebSocketApiException ex =
        assertThrows(
            WebSocketApiException.class, () -> authService.login("unknown", "password123"));

    assertEquals("Record not found", ex.getMessage());
  }

  @Test
  void registerRejectsShortPassword() {
    assertThrows(
        IllegalArgumentException.class,
        () -> authService.register("testuser", "test@example.com", "short"));
  }
}
