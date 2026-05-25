package ua.notion.domain.service.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuthPayloadsTest {

  @Test
  void loginPayloadUsesUsername() {
    var payload = AuthPayloads.loginPayload("john_doe", "secret123");

    assertEquals("john_doe", payload.get("username"));
    assertEquals("secret123", payload.get("password"));
    assertNull(payload.get("email"));
  }

  @Test
  void loginPayloadUsesEmail() {
    var payload = AuthPayloads.loginPayload("john@example.com", "secret123");

    assertEquals("john@example.com", payload.get("email"));
    assertEquals("secret123", payload.get("password"));
    assertNull(payload.get("username"));
  }

  @Test
  void loginPayloadTrimsIdentifier() {
    var payload = AuthPayloads.loginPayload("  john@example.com  ", "secret123");

    assertEquals("john@example.com", payload.get("email"));
  }
}
