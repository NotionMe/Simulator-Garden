package ua.notion.domain.service.auth;

import java.util.Map;

/** Builds HTTP auth payloads per WEBSOCKET_API.md (username or email, never both). */
public final class AuthPayloads {

  private AuthPayloads() {}

  public static Map<String, String> loginPayload(String identifier, String password) {
    String trimmed = identifier.trim();
    if (trimmed.contains("@")) {
      return Map.of("email", trimmed, "password", password);
    }
    return Map.of("username", trimmed, "password", password);
  }
}
