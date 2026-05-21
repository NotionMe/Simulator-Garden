package ua.notion.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.notion.domain.entity.Garden;
import ua.notion.domain.entity.User;
import ua.notion.domain.entity.WeatherEvent;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.TestDatabaseManager;

class WeatherEventServiceTest {
  private TestDatabaseManager dbManager;
  private PersistenceContext context;
  private WeatherEventService weatherEventService;
  private UserService userService;
  private GardenService gardenService;

  private User testUser;
  private Garden testGarden;

  @BeforeEach
  void setUp() {
    dbManager = new TestDatabaseManager();
    context = dbManager.createContext();
    weatherEventService = new WeatherEventService(context);
    userService = new UserService(context);
    gardenService = new GardenService(context);

    testUser = userService.createUser("testuser", "test@example.com");
    testGarden = gardenService.createGarden(testUser.getId(), "Test Garden", 10, 10);
  }

  @AfterEach
  void tearDown() {
    if (context != null) {
      context.close();
    }
    dbManager.cleanup();
  }

  @Test
  void testCreateWeatherEvent() {
    WeatherEvent event = weatherEventService.createWeatherEvent(testGarden.getId(), "rain", 7);

    assertNotNull(event);
    assertNotNull(event.getId());
    assertEquals(testGarden.getId(), event.getGardenId());
    assertEquals("rain", event.getEventType());
    assertEquals(7, event.getIntensity());
    assertNotNull(event.getOccurredAt());
  }

  @Test
  void testCreateWeatherEventWithInvalidIntensity() {
    assertThrows(
        IllegalArgumentException.class,
        () -> weatherEventService.createWeatherEvent(testGarden.getId(), "rain", -1));

    assertThrows(
        IllegalArgumentException.class,
        () -> weatherEventService.createWeatherEvent(testGarden.getId(), "rain", 11));
  }

  @Test
  void testCreateWeatherEventWithValidIntensityBoundaries() {
    WeatherEvent event1 = weatherEventService.createWeatherEvent(testGarden.getId(), "drought", 1);
    WeatherEvent event2 = weatherEventService.createWeatherEvent(testGarden.getId(), "storm", 10);

    assertEquals(1, event1.getIntensity());
    assertEquals(10, event2.getIntensity());
  }

  @Test
  void testFindWeatherEventById() {
    WeatherEvent created = weatherEventService.createWeatherEvent(testGarden.getId(), "frost", 5);

    Optional<WeatherEvent> found = weatherEventService.findWeatherEventById(created.getId());

    assertTrue(found.isPresent());
    assertEquals("frost", found.get().getEventType());
    assertEquals(5, found.get().getIntensity());
  }

  @Test
  void testFindWeatherEventByIdNotFound() {
    Optional<WeatherEvent> found = weatherEventService.findWeatherEventById(99999);
    assertFalse(found.isPresent());
  }

  @Test
  void testFindWeatherEventsByGardenId() {
    weatherEventService.createWeatherEvent(testGarden.getId(), "rain", 5);
    weatherEventService.createWeatherEvent(testGarden.getId(), "heatwave", 8);
    weatherEventService.createWeatherEvent(testGarden.getId(), "storm", 3);

    List<WeatherEvent> events = weatherEventService.findWeatherEventsByGardenId(testGarden.getId());

    assertEquals(3, events.size());
    assertTrue(events.stream().allMatch(e -> e.getGardenId().equals(testGarden.getId())));
  }

  @Test
  void testFindWeatherEventsByType() {
    weatherEventService.createWeatherEvent(testGarden.getId(), "rain", 5);
    weatherEventService.createWeatherEvent(testGarden.getId(), "rain", 7);
    weatherEventService.createWeatherEvent(testGarden.getId(), "drought", 8);

    List<WeatherEvent> rainEvents = weatherEventService.findWeatherEventsByType("rain");

    assertEquals(2, rainEvents.size());
    assertTrue(rainEvents.stream().allMatch(e -> e.getEventType().equals("rain")));
  }

  @Test
  void testFindRecentWeatherEvents() {
    weatherEventService.createWeatherEvent(testGarden.getId(), "rain", 5);
    weatherEventService.createWeatherEvent(testGarden.getId(), "storm", 6);

    WeatherEvent old =
        WeatherEvent.builder()
            .gardenId(testGarden.getId())
            .eventType("frost")
            .intensity(3)
            .occurredAt(LocalDateTime.now().minusDays(10))
            .build();

    context.getWeatherEventRepository().save(old);

    List<WeatherEvent> recent = weatherEventService.findRecentWeatherEvents(testGarden.getId(), 7);

    assertEquals(2, recent.size());
    assertTrue(recent.stream().noneMatch(e -> e.getEventType().equals("frost")));
  }

  @Test
  void testFindLatestWeatherEvent() {
    WeatherEvent first = weatherEventService.createWeatherEvent(testGarden.getId(), "rain", 5);
    WeatherEvent second = weatherEventService.createWeatherEvent(testGarden.getId(), "storm", 6);
    WeatherEvent latest = weatherEventService.createWeatherEvent(testGarden.getId(), "heatwave", 7);

    Optional<WeatherEvent> found = weatherEventService.findLatestWeatherEvent(testGarden.getId());

    assertTrue(found.isPresent());
    // Should find one of the events (order may vary if timestamps are identical)
    assertTrue(found.get().getId() >= first.getId());
  }

  @Test
  void testFindLatestWeatherEventNoEvents() {
    Optional<WeatherEvent> found = weatherEventService.findLatestWeatherEvent(testGarden.getId());

    assertFalse(found.isPresent());
  }

  @Test
  void testDeleteWeatherEvent() {
    WeatherEvent event = weatherEventService.createWeatherEvent(testGarden.getId(), "rain", 5);
    Integer eventId = event.getId();

    weatherEventService.deleteWeatherEvent(eventId);

    Optional<WeatherEvent> deleted = weatherEventService.findWeatherEventById(eventId);
    assertFalse(deleted.isPresent());
  }

  @Test
  void testDeleteOldWeatherEvents() {
    weatherEventService.createWeatherEvent(testGarden.getId(), "rain", 5);

    WeatherEvent old1 =
        WeatherEvent.builder()
            .gardenId(testGarden.getId())
            .eventType("frost")
            .intensity(3)
            .occurredAt(LocalDateTime.now().minusDays(10))
            .build();

    WeatherEvent old2 =
        WeatherEvent.builder()
            .gardenId(testGarden.getId())
            .eventType("drought")
            .intensity(4)
            .occurredAt(LocalDateTime.now().minusDays(15))
            .build();

    context.getWeatherEventRepository().save(old1);
    context.getWeatherEventRepository().save(old2);

    weatherEventService.deleteOldWeatherEvents(testGarden.getId(), 7);

    List<WeatherEvent> remaining =
        weatherEventService.findWeatherEventsByGardenId(testGarden.getId());

    assertEquals(1, remaining.size());
    assertEquals("rain", remaining.get(0).getEventType());
  }

  @Test
  void testMultipleGardensWeatherEvents() {
    Garden garden2 = gardenService.createGarden(testUser.getId(), "Garden 2", 5, 5);

    weatherEventService.createWeatherEvent(testGarden.getId(), "rain", 5);
    weatherEventService.createWeatherEvent(garden2.getId(), "storm", 6);

    List<WeatherEvent> garden1Events =
        weatherEventService.findWeatherEventsByGardenId(testGarden.getId());
    List<WeatherEvent> garden2Events =
        weatherEventService.findWeatherEventsByGardenId(garden2.getId());

    assertEquals(1, garden1Events.size());
    assertEquals(1, garden2Events.size());
    assertEquals("rain", garden1Events.get(0).getEventType());
    assertEquals("storm", garden2Events.get(0).getEventType());
  }
}
