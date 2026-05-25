package ua.notion.domain.service.auth;

import java.util.Optional;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.websocket.AuthSession;
import ua.notion.infrastructure.websocket.WebSocketApiClient;
import ua.notion.infrastructure.websocket.WebSocketApiException;

/** Authentication via HTTP ({@code /api/login}, {@code /api/register}), then WebSocket with JWT. */
public class WebSocketAuthenticationService implements AuthenticationService {

  private final WebSocketApiClient apiClient;

  public WebSocketAuthenticationService(WebSocketApiClient apiClient) {
    this.apiClient = apiClient;
  }

  @Override
  public User register(String username, String email, String password)
      throws WebSocketApiException {
    AuthValidation.validateRegistration(username, email, password);
    return apiClient.registerAccount(username, email, password).user();
  }

  @Override
  public Optional<User> login(String identifier, String password) throws WebSocketApiException {
    AuthValidation.validateLoginInput(identifier, password);
    AuthSession session = apiClient.loginAccount(identifier.trim(), password);
    return Optional.ofNullable(session.user());
  }
}
