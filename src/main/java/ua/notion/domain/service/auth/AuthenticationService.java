package ua.notion.domain.service.auth;

import java.util.Optional;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.websocket.WebSocketApiException;

/**
 * Client authentication via HTTP ({@code POST /api/register}, {@code POST /api/login}), then
 * WebSocket with the returned JWT. The server hashes passwords; the client sends plain text only.
 */
public interface AuthenticationService {

  User register(String username, String email, String password) throws WebSocketApiException;

  /**
   * Log in with username or email and a plain-text password.
   *
   * @param identifier username or email (see WEBSOCKET_API.md)
   * @param password plain-text password
   * @return authenticated user on success
   * @throws WebSocketApiException when the server returns an error (e.g. invalid credentials)
   */
  Optional<User> login(String identifier, String password) throws WebSocketApiException;

  /**
   * @deprecated Use {@link #login(String, String)}
   */
  @Deprecated
  default Optional<User> authenticate(String identifier, String password)
      throws WebSocketApiException {
    return login(identifier, password);
  }
}
