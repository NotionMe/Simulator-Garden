package ua.notion.infrastructure.websocket;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Client for the Rust server WebSocket CRUD API. */
public class WebSocketApiClient implements AutoCloseable {

  public static final String DEFAULT_URL = "wss://rustserver-stan.azurewebsites.net/server/ws";
  private static final long DEFAULT_TIMEOUT_SECONDS = 10;

  private final URI serverUri;
  private final HttpClient httpClient;
  private final Gson gson;
  private final Map<String, CompletableFuture<ApiResponse>> pendingRequests =
      new ConcurrentHashMap<>();

  private volatile WebSocket webSocket;
  private volatile CompletableFuture<Void> connectedFuture;

  public WebSocketApiClient() {
    this(System.getProperty("server.ws.url", DEFAULT_URL));
  }

  public WebSocketApiClient(String serverUrl) {
    this.serverUri = URI.create(Objects.requireNonNull(serverUrl, "serverUrl cannot be null"));
    this.httpClient = HttpClient.newHttpClient();
    this.gson =
        new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
  }

  public synchronized CompletableFuture<Void> connectAsync() {
    if (webSocket != null && !webSocket.isInputClosed() && !webSocket.isOutputClosed()) {
      return CompletableFuture.completedFuture(null);
    }

    connectedFuture = new CompletableFuture<>();
    httpClient
        .newWebSocketBuilder()
        .buildAsync(serverUri, new Listener())
        .whenComplete(
            (socket, error) -> {
              if (error != null) {
                connectedFuture.completeExceptionally(error);
              } else {
                webSocket = socket;
                connectedFuture.complete(null);
              }
            });

    return connectedFuture;
  }

  public void connect() {
    await(connectAsync(), "Failed to connect to server WebSocket");
  }

  public <T> T send(String command, String reason, Object payload, Class<T> responseType) {
    JsonElement data = sendForJson(command, reason, payload);
    if (data == null || data.isJsonNull()) {
      return null;
    }
    return gson.fromJson(data, responseType);
  }

  public <T> T send(String command, String reason, Object payload, Type responseType) {
    JsonElement data = sendForJson(command, reason, payload);
    if (data == null || data.isJsonNull()) {
      return null;
    }
    return gson.fromJson(data, responseType);
  }

  public JsonElement sendForJson(String command, String reason, Object payload) {
    ensureConnected();

    String requestId = "java-" + UUID.randomUUID();
    ApiRequest request =
        new ApiRequest(requestId, command, reason, payload == null ? new JsonObject() : payload);
    CompletableFuture<ApiResponse> responseFuture = new CompletableFuture<>();
    pendingRequests.put(requestId, responseFuture);

    try {
      webSocket.sendText(gson.toJson(request), true).join();
      ApiResponse response = responseFuture.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

      if (!"success".equalsIgnoreCase(response.status())) {
        throw new WebSocketApiException(
            response.error() == null ? "Server returned error" : response.error());
      }

      return response.data();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new WebSocketApiException("Interrupted while waiting for server response", e);
    } catch (ExecutionException | TimeoutException e) {
      throw new WebSocketApiException("Failed to receive server response", e);
    } finally {
      pendingRequests.remove(requestId);
    }
  }

  private void ensureConnected() {
    WebSocket socket = webSocket;
    if (socket == null || socket.isInputClosed() || socket.isOutputClosed()) {
      connect();
    } else if (connectedFuture != null && !connectedFuture.isDone()) {
      await(connectedFuture, "Failed to connect to server WebSocket");
    }
  }

  private void await(CompletableFuture<Void> future, String errorMessage) {
    try {
      future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new WebSocketApiException(errorMessage, e);
    } catch (ExecutionException | TimeoutException e) {
      throw new WebSocketApiException(errorMessage, e);
    }
  }

  @Override
  public synchronized void close() throws IOException {
    if (webSocket != null) {
      webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client shutdown").join();
      webSocket = null;
    }
    pendingRequests
        .values()
        .forEach(f -> f.completeExceptionally(new IOException("Client closed")));
    pendingRequests.clear();
  }

  private final class Listener implements WebSocket.Listener {

    private final StringBuilder partialMessage = new StringBuilder();

    @Override
    public void onOpen(WebSocket webSocket) {
      webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
      partialMessage.append(data);
      if (last) {
        String message = partialMessage.toString();
        partialMessage.setLength(0);
        handleMessage(message);
      }
      webSocket.request(1);
      return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
      pendingRequests.values().forEach(f -> f.completeExceptionally(error));
      pendingRequests.clear();
    }
  }

  private void handleMessage(String message) {
    ApiResponse response = gson.fromJson(message, ApiResponse.class);
    CompletableFuture<ApiResponse> future = pendingRequests.get(response.requestId());
    if (future != null) {
      future.complete(response);
    }
  }

  private record ApiRequest(String request_id, String command, String reason, Object payload) {}

  private record ApiResponse(
      String request_id, String status, String command, JsonElement data, String error) {
    String requestId() {
      return request_id;
    }
  }

  private static final class LocalDateTimeAdapter
      implements com.google.gson.JsonSerializer<LocalDateTime>,
          com.google.gson.JsonDeserializer<LocalDateTime> {

    @Override
    public JsonElement serialize(
        LocalDateTime src, Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
      java.time.ZonedDateTime utcDateTime =
          src.atZone(java.time.ZoneId.systemDefault())
              .withZoneSameInstant(java.time.ZoneId.of("UTC"));
      return new JsonPrimitive(
          utcDateTime.format(
              java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));
    }

    @Override
    public LocalDateTime deserialize(
        JsonElement json, Type typeOfT, com.google.gson.JsonDeserializationContext context) {
      if (json == null || json.isJsonNull()) {
        return null;
      }
      String str = json.getAsString();
      try {
        if (str.endsWith("Z")) {
          return LocalDateTime.ofInstant(java.time.Instant.parse(str), java.time.ZoneId.of("UTC"));
        }
        if (str.contains("+") || (str.contains("-") && str.lastIndexOf("-") > 10)) {
          return java.time.OffsetDateTime.parse(str).toLocalDateTime();
        }
        return LocalDateTime.parse(str);
      } catch (Exception e) {
        try {
          return java.time.OffsetDateTime.parse(str).toLocalDateTime();
        } catch (Exception e2) {
          try {
            return LocalDateTime.ofInstant(
                java.time.Instant.parse(str), java.time.ZoneId.of("UTC"));
          } catch (Exception e3) {
            return LocalDateTime.parse(str);
          }
        }
      }
    }
  }

  private static final class LocalDateAdapter
      implements com.google.gson.JsonSerializer<LocalDate>,
          com.google.gson.JsonDeserializer<LocalDate> {

    @Override
    public JsonElement serialize(
        LocalDate src, Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
      return new JsonPrimitive(
          src.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00")));
    }

    @Override
    public LocalDate deserialize(
        JsonElement json, Type typeOfT, com.google.gson.JsonDeserializationContext context) {
      if (json == null || json.isJsonNull()) {
        return null;
      }
      String value = json.getAsString();
      return value.length() > 10
          ? LocalDateTime.parse(value).toLocalDate()
          : LocalDate.parse(value);
    }
  }
}
