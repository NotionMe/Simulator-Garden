package ua.notion.infrastructure.websocket;

public class WebSocketApiException extends RuntimeException {
  public WebSocketApiException(String message) {
    super(message);
  }

  public WebSocketApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
