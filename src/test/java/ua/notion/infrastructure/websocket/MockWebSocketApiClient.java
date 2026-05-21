package ua.notion.infrastructure.websocket;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import ua.notion.domain.entity.User;

public class MockWebSocketApiClient extends WebSocketApiClient {

  private final Gson mockGson;
  private final Map<String, List<Object>> databases = new ConcurrentHashMap<>();
  private final Map<String, AtomicInteger> idGenerators = new ConcurrentHashMap<>();

  private static final Set<String> VALID_CLIMATE_TYPES =
      new HashSet<>(Arrays.asList("tropical", "temperate", "arid", "cold", "mediterranean"));

  private static final Set<String> VALID_TASK_TYPES =
      new HashSet<>(Arrays.asList("water", "fertilize", "prune", "harvest", "pest_control"));

  private static final Set<String> VALID_WEATHER_EVENTS =
      new HashSet<>(Arrays.asList("sun", "rain", "storm", "drought", "frost", "heatwave"));

  public MockWebSocketApiClient() {
    super("ws://mock-server/ws");
    this.mockGson =
        new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
  }

  @Override
  public synchronized CompletableFuture<Void> connectAsync() {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void connect() {
    // No-op
  }

  @Override
  public void close() {
    // No-op
  }

  @Override
  public <T> T send(String command, String reason, Object payload, Class<T> responseType) {
    return send(command, reason, payload, (Type) responseType);
  }

  @Override
  public <T> T send(String command, String reason, Object payload, Type responseType) {
    Object result = processCommand(command, reason, payload);
    if (result == null) {
      return null;
    }
    String json = mockGson.toJson(result);
    return mockGson.fromJson(json, responseType);
  }

  @Override
  public JsonElement sendForJson(String command, String reason, Object payload) {
    Object result = processCommand(command, reason, payload);
    return mockGson.toJsonTree(result);
  }

  private synchronized Object processCommand(String command, String reason, Object payload) {
    List<Object> list = databases.computeIfAbsent(reason, k -> new ArrayList<>());
    AtomicInteger idGen = idGenerators.computeIfAbsent(reason, k -> new AtomicInteger(1));

    switch (command) {
      case "create":
        {
          // If a Map is passed for "user", convert to User (mock stores password internally).
          Object entity = payload;
          if ("user".equals(reason) && payload instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) payload;
            User u = new User();
            u.setUsername((String) map.get("username"));
            u.setEmail((String) map.get("email"));
            u.setPasswordHash((String) map.get("password"));
            u.setCreatedAt(LocalDateTime.now());
            entity = u;
          }

          // Validate constraints
          validateEntityConstraints(reason, entity, list);

          // Set ID
          try {
            Field idField = findField(entity.getClass(), "id");
            if (idField != null) {
              idField.setAccessible(true);
              if (idField.get(entity) == null) {
                idField.set(entity, idGen.getAndIncrement());
              }
            }
          } catch (Exception e) {
            throw new RuntimeException("Failed to set ID", e);
          }

          list.add(entity);
          return stripUserSecrets(entity);
        }

      case "login":
        {
          if (!"user".equals(reason)) {
            throw new UnsupportedOperationException(
                "login command is only supported for user resource");
          }

          @SuppressWarnings("unchecked")
          Map<String, Object> loginPayload = (Map<String, Object>) payload;
          String password = (String) loginPayload.get("password");
          String loginUsername = (String) loginPayload.get("username");
          String loginEmail = (String) loginPayload.get("email");

          // Find user by username or email
          Object found = null;
          for (Object item : list) {
            if (loginUsername != null) {
              Object storedUsername = getFieldValue(item, "username");
              if (loginUsername.equals(storedUsername)) {
                found = item;
                break;
              }
            } else if (loginEmail != null) {
              Object storedEmail = getFieldValue(item, "email");
              if (loginEmail.equals(storedEmail)) {
                found = item;
                break;
              }
            }
          }

          if (found == null) {
            throw new WebSocketApiException("Record not found");
          }

          String storedPassword = (String) getFieldValue(found, "passwordHash");
          if (storedPassword == null || !storedPassword.equals(password)) {
            throw new WebSocketApiException("Invalid credentials");
          }

          return stripUserSecrets(found);
        }

      case "read":
        {
          Integer id = extractId(payload);
          if (id == null) {
            throw new IllegalArgumentException("Missing id for read command");
          }
          return list.stream()
              .filter(item -> id.equals(getFieldValue(item, "id")))
              .findFirst()
              .orElse(null);
        }

      case "update":
        {
          Integer id = null;
          if (payload instanceof Map) {
            id = ((Number) ((Map<?, ?>) payload).get("id")).intValue();
          } else {
            id = (Integer) getFieldValue(payload, "id");
          }

          if (id == null) {
            throw new IllegalArgumentException("Missing id for update command");
          }

          // Find existing
          final Integer finalId = id;
          Object existing =
              list.stream()
                  .filter(item -> finalId.equals(getFieldValue(item, "id")))
                  .findFirst()
                  .orElseThrow(() -> new RuntimeException("Entity not found for update"));

          // Validate constraints excluding the current element itself
          List<Object> otherEntities = new ArrayList<>(list);
          otherEntities.remove(existing);
          validateEntityConstraints(reason, payload, otherEntities);

          // Replace in list
          list.remove(existing);
          list.add(payload);
          return payload;
        }

      case "delete":
        {
          Integer id = extractId(payload);
          if (id == null) {
            throw new IllegalArgumentException("Missing id for delete command");
          }
          boolean removed = list.removeIf(item -> id.equals(getFieldValue(item, "id")));
          if (removed) {
            cascadeDelete(reason, id);
          }
          JsonObject response = new JsonObject();
          response.addProperty("deleted", removed);
          return response;
        }

      case "list":
        {
          if (payload instanceof Map) {
            Map<?, ?> filters = (Map<?, ?>) payload;
            List<Object> filtered = new ArrayList<>();
            for (Object item : list) {
              boolean matches = true;
              for (Map.Entry<?, ?> entry : filters.entrySet()) {
                String fieldName = String.valueOf(entry.getKey());
                Object expectedVal = entry.getValue();
                Object actualVal = getFieldValue(item, convertToCamelCase(fieldName));
                if (actualVal == null && expectedVal == null) {
                  continue;
                }
                if (actualVal == null || expectedVal == null) {
                  matches = false;
                  break;
                }
                if (!String.valueOf(actualVal).equals(String.valueOf(expectedVal))) {
                  matches = false;
                  break;
                }
              }
              if (matches) {
                filtered.add(item);
              }
            }
            return filtered;
          }
          return list;
        }

      default:
        throw new UnsupportedOperationException("Unknown command: " + command);
    }
  }

  private void validateEntityConstraints(String reason, Object entity, List<Object> list) {
    if ("user".equals(reason)) {
      String username = (String) getFieldValue(entity, "username");
      String email = (String) getFieldValue(entity, "email");
      for (Object existing : list) {
        if (username != null
            && username.equalsIgnoreCase((String) getFieldValue(existing, "username"))) {
          throw new RuntimeException("Unique constraint violation: username already exists");
        }
        if (email != null && email.equalsIgnoreCase((String) getFieldValue(existing, "email"))) {
          throw new RuntimeException("Unique constraint violation: email already exists");
        }
      }
    } else if ("garden".equals(reason)) {
      Integer userId = (Integer) getFieldValue(entity, "userId");
      String name = (String) getFieldValue(entity, "name");
      Integer width = (Integer) getFieldValue(entity, "widthCells");
      Integer height = (Integer) getFieldValue(entity, "heightCells");

      if (width != null && (width <= 0 || width > 100)) {
        throw new RuntimeException("Constraint violation: width must be between 1 and 100");
      }
      if (height != null && (height <= 0 || height > 100)) {
        throw new RuntimeException("Constraint violation: height must be between 1 and 100");
      }

      for (Object existing : list) {
        if (userId != null
            && userId.equals(getFieldValue(existing, "userId"))
            && name != null
            && name.equalsIgnoreCase((String) getFieldValue(existing, "name"))) {
          throw new RuntimeException(
              "Unique constraint violation: garden name already exists for user");
        }
      }
    } else if ("plant".equals(reason)) {
      Integer growthDays = (Integer) getFieldValue(entity, "growthDays");
      String climateType = (String) getFieldValue(entity, "climateType");

      if (growthDays != null && growthDays <= 0) {
        throw new RuntimeException("Constraint violation: growth days must be positive");
      }
      if (climateType != null && !VALID_CLIMATE_TYPES.contains(climateType.toLowerCase())) {
        throw new RuntimeException("Constraint violation: invalid climate type");
      }
    } else if ("plantinstance".equals(reason)) {
      Integer gardenId = (Integer) getFieldValue(entity, "gardenId");
      Integer cellX = (Integer) getFieldValue(entity, "cellX");
      Integer cellY = (Integer) getFieldValue(entity, "cellY");
      Integer stage = (Integer) getFieldValue(entity, "growthStage");

      if (stage != null && (stage < 0 || stage > 100)) {
        throw new RuntimeException("Constraint violation: growth stage must be between 0 and 100");
      }

      for (Object existing : list) {
        if (gardenId != null
            && gardenId.equals(getFieldValue(existing, "gardenId"))
            && cellX != null
            && cellX.equals(getFieldValue(existing, "cellX"))
            && cellY != null
            && cellY.equals(getFieldValue(existing, "cellY"))) {
          throw new RuntimeException(
              "Unique constraint violation: cell already occupied in this garden");
        }
      }
    } else if ("task".equals(reason)) {
      String type = (String) getFieldValue(entity, "taskType");
      if (type != null && !VALID_TASK_TYPES.contains(type.toLowerCase())) {
        throw new RuntimeException("Constraint violation: invalid task type");
      }
    } else if ("weatherevent".equals(reason)) {
      String type = (String) getFieldValue(entity, "eventType");
      Integer intensity = (Integer) getFieldValue(entity, "intensity");

      if (type != null && !VALID_WEATHER_EVENTS.contains(type.toLowerCase())) {
        throw new RuntimeException("Constraint violation: invalid weather event type");
      }
      if (intensity != null && (intensity < 1 || intensity > 10)) {
        throw new RuntimeException("Constraint violation: intensity must be between 1 and 10");
      }
    } else if ("achievement".equals(reason)) {
      Integer userId = (Integer) getFieldValue(entity, "userId");
      String key = (String) getFieldValue(entity, "conditionKey");

      for (Object existing : list) {
        if (userId != null
            && userId.equals(getFieldValue(existing, "userId"))
            && key != null
            && key.equalsIgnoreCase((String) getFieldValue(existing, "conditionKey"))) {
          throw new RuntimeException(
              "Unique constraint violation: achievement condition key already exists for user");
        }
      }
    }
  }

  private void cascadeDelete(String reason, Integer deletedId) {
    if ("user".equals(reason)) {
      // Delete achievements
      List<Object> achievements = databases.get("achievement");
      if (achievements != null) {
        achievements.removeIf(a -> deletedId.equals(getFieldValue(a, "userId")));
      }
      // Delete gardens (recursively cascade)
      List<Object> gardens = databases.get("garden");
      if (gardens != null) {
        List<Integer> gardenIdsToDelete = new ArrayList<>();
        for (Object g : gardens) {
          if (deletedId.equals(getFieldValue(g, "userId"))) {
            gardenIdsToDelete.add((Integer) getFieldValue(g, "id"));
          }
        }
        gardens.removeIf(g -> deletedId.equals(getFieldValue(g, "userId")));
        for (Integer gid : gardenIdsToDelete) {
          cascadeDelete("garden", gid);
        }
      }
    } else if ("garden".equals(reason)) {
      // Delete weather events
      List<Object> events = databases.get("weatherevent");
      if (events != null) {
        events.removeIf(e -> deletedId.equals(getFieldValue(e, "gardenId")));
      }
      // Delete plant instances (recursively cascade)
      List<Object> instances = databases.get("plantinstance");
      if (instances != null) {
        List<Integer> instanceIdsToDelete = new ArrayList<>();
        for (Object pi : instances) {
          if (deletedId.equals(getFieldValue(pi, "gardenId"))) {
            instanceIdsToDelete.add((Integer) getFieldValue(pi, "id"));
          }
        }
        instances.removeIf(pi -> deletedId.equals(getFieldValue(pi, "gardenId")));
        for (Integer piid : instanceIdsToDelete) {
          cascadeDelete("plantinstance", piid);
        }
      }
    } else if ("plantinstance".equals(reason)) {
      // Delete tasks
      List<Object> tasks = databases.get("task");
      if (tasks != null) {
        tasks.removeIf(t -> deletedId.equals(getFieldValue(t, "plantInstanceId")));
      }
    }
  }

  private Integer extractId(Object payload) {
    if (payload instanceof Map) {
      Object idVal = ((Map<?, ?>) payload).get("id");
      if (idVal instanceof Number) {
        return ((Number) idVal).intValue();
      }
    } else if (payload instanceof JsonObject) {
      JsonElement idVal = ((JsonObject) payload).get("id");
      if (idVal != null && idVal.isJsonPrimitive()) {
        return idVal.getAsInt();
      }
    }
    return null;
  }

  private Object getFieldValue(Object obj, String fieldName) {
    try {
      Field field = findField(obj.getClass(), fieldName);
      if (field != null) {
        field.setAccessible(true);
        return field.get(obj);
      }
    } catch (Exception e) {
      // Ignore
    }
    return null;
  }

  /** API responses never include password fields. */
  private Object stripUserSecrets(Object entity) {
    if (!(entity instanceof User user)) {
      return entity;
    }
    User response = new User();
    response.setId(user.getId());
    response.setUsername(user.getUsername());
    response.setEmail(user.getEmail());
    response.setCreatedAt(user.getCreatedAt());
    return response;
  }

  private Field findField(Class<?> clazz, String fieldName) {
    Class<?> current = clazz;
    while (current != null) {
      try {
        return current.getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        current = current.getSuperclass();
      }
    }
    return null;
  }

  private String convertToCamelCase(String snakeCase) {
    if (snakeCase == null || snakeCase.isEmpty()) {
      return snakeCase;
    }
    StringBuilder sb = new StringBuilder();
    boolean nextUpper = false;
    for (int i = 0; i < snakeCase.length(); i++) {
      char c = snakeCase.charAt(i);
      if (c == '_') {
        nextUpper = true;
      } else {
        if (nextUpper) {
          sb.append(Character.toUpperCase(c));
          nextUpper = false;
        } else {
          sb.append(c);
        }
      }
    }
    return sb.toString();
  }

  private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value.format(formatter));
      }
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
      if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return LocalDateTime.parse(in.nextString(), formatter);
    }
  }

  private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value.format(formatter));
      }
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
      if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return LocalDate.parse(in.nextString(), formatter);
    }
  }
}
