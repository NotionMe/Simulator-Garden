package ua.notion.domain.service.auth;

import java.util.Map;
import java.util.Optional;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.websocket.WebSocketApiClient;
import ua.notion.infrastructure.websocket.WebSocketApiException;

/**
 * Authentication via the server WebSocket API ({@code login} / {@code create} on {@code user}).
 *
 * @see ua.notion.infrastructure.websocket.WebSocketApiClient
 */
public class WebSocketAuthenticationService implements AuthenticationService {

  private final WebSocketApiClient apiClient;

  public WebSocketAuthenticationService(WebSocketApiClient apiClient) {
    this.apiClient = apiClient;
  }

  @Override
  public User register(String username, String email, String password)
      throws WebSocketApiException {
    AuthValidation.validateRegistration(username, email, password);

    return apiClient.send(
        "create",
        "user",
        Map.of("username", username, "email", email, "password", password),
        User.class);
  }

  @Override
  public Optional<User> login(String identifier, String password) throws WebSocketApiException {
    AuthValidation.validateLoginInput(identifier, password);

    User user =
        apiClient.send("login", "user", buildLoginPayload(identifier.trim(), password), User.class);
    return Optional.ofNullable(user);
  }

  /**
   * Builds a login payload per WEBSOCKET_API.md: either {@code username} or {@code email}, never
   * both, plus plain {@code password}.
   */
  static Map<String, String> buildLoginPayload(String identifier, String password) {
    if (identifier.contains("@")) {
      return Map.of("email", identifier, "password", password);
    }
    return Map.of("username", identifier, "password", password);
  }
}
