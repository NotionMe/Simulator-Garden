package ua.notion.domain.service.auth;

import java.util.Optional;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.websocket.WebSocketApiException;

/**
 * Client authentication API aligned with the WebSocket {@code user} resource.
 *
 * <p>Login uses {@code command: "login"} with {@code username} or {@code email} plus plain {@code
 * password} in the payload. Registration uses {@code command: "create"}. The server hashes
 * passwords; the client must not hash them before sending.
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
