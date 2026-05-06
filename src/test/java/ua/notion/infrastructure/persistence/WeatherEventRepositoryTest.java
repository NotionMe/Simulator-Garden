package ua.notion.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import ua.notion.domain.entity.Garden;
import ua.notion.domain.entity.User;
import ua.notion.domain.entity.WeatherEvent;
import ua.notion.infrastructure.persistence.contract.GardenRepository;
import ua.notion.infrastructure.persistence.contract.UserRepository;
import ua.notion.infrastructure.persistence.contract.WeatherEventRepository;

class WeatherEventRepositoryTest extends BaseRepositoryTest {

  @Test
  void shouldSaveAndFindWeatherEventById() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();
    WeatherEventRepository eventRepo = context.getWeatherEventRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
                .createdAt(LocalDateTime.now())
                .build());

    Garden garden =
        gardenRepo.save(
            Garden.builder()
                .userId(user.getId())
                .name("Garden")
                .widthCells(10)
                .heightCells(10)
                .createdAt(LocalDateTime.now())
                .build());

    WeatherEvent event =
        WeatherEvent.builder()
            .gardenId(garden.getId())
            .eventType("rain")
            .intensity(5)
            .occurredAt(LocalDateTime.now())
            .build();

    WeatherEvent saved = eventRepo.save(event);

    assertNotNull(saved.getId());
    assertEquals("rain", saved.getEventType());
    assertEquals(5, saved.getIntensity());

    Optional<WeatherEvent> found = eventRepo.findById(saved.getId());
    assertTrue(found.isPresent());
    assertEquals("rain", found.get().getEventType());
  }

  @Test
  void shouldUpdateWeatherEvent() {
    WeatherEventRepository eventRepo = context.getWeatherEventRepository();
    Garden garden = createGarden();

    WeatherEvent event =
        eventRepo.save(
            WeatherEvent.builder()
                .gardenId(garden.getId())
                .eventType("drought")
                .intensity(3)
                .occurredAt(LocalDateTime.now())
                .build());

    event.setIntensity(7);
    event.setEventType("heatwave");

    eventRepo.update(event.getId(), event);

    Optional<WeatherEvent> updated = eventRepo.findById(event.getId());
    assertTrue(updated.isPresent());
    assertEquals(7, updated.get().getIntensity());
    assertEquals("heatwave", updated.get().getEventType());
  }

  @Test
  void shouldDeleteWeatherEvent() {
    WeatherEventRepository eventRepo = context.getWeatherEventRepository();
    Garden garden = createGarden();

    WeatherEvent event =
        eventRepo.save(
            WeatherEvent.builder()
                .gardenId(garden.getId())
                .eventType("frost")
                .intensity(8)
                .occurredAt(LocalDateTime.now())
                .build());

    Integer id = event.getId();
    eventRepo.delete(id);

    Optional<WeatherEvent> deleted = eventRepo.findById(id);
    assertFalse(deleted.isPresent());
  }

  @Test
  void shouldEnforceEventTypeConstraint() {
    WeatherEventRepository eventRepo = context.getWeatherEventRepository();
    Garden garden = createGarden();

    assertThrows(
        Exception.class,
        () ->
            eventRepo.save(
                WeatherEvent.builder()
                    .gardenId(garden.getId())
                    .eventType("invalid_event")
                    .intensity(5)
                    .occurredAt(LocalDateTime.now())
                    .build()));
  }

  @Test
  void shouldEnforceIntensityConstraint() {
    WeatherEventRepository eventRepo = context.getWeatherEventRepository();
    Garden garden = createGarden();

    assertThrows(
        Exception.class,
        () ->
            eventRepo.save(
                WeatherEvent.builder()
                    .gardenId(garden.getId())
                    .eventType("rain")
                    .intensity(0)
                    .occurredAt(LocalDateTime.now())
                    .build()));

    assertThrows(
        Exception.class,
        () ->
            eventRepo.save(
                WeatherEvent.builder()
                    .gardenId(garden.getId())
                    .eventType("rain")
                    .intensity(11)
                    .occurredAt(LocalDateTime.now())
                    .build()));
  }

  @Test
  void shouldCascadeDeleteWhenGardenDeleted() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();
    WeatherEventRepository eventRepo = context.getWeatherEventRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
                .createdAt(LocalDateTime.now())
                .build());

    Garden garden =
        gardenRepo.save(
            Garden.builder()
                .userId(user.getId())
                .name("Garden")
                .widthCells(10)
                .heightCells(10)
                .createdAt(LocalDateTime.now())
                .build());

    WeatherEvent event =
        eventRepo.save(
            WeatherEvent.builder()
                .gardenId(garden.getId())
                .eventType("storm")
                .intensity(9)
                .occurredAt(LocalDateTime.now())
                .build());

    Integer eventId = event.getId();
    gardenRepo.delete(garden.getId());

    Optional<WeatherEvent> deleted = eventRepo.findById(eventId);
    assertFalse(deleted.isPresent());
  }

  @Test
  void shouldFindEventsByGardenId() {
    WeatherEventRepository eventRepo = context.getWeatherEventRepository();
    Garden garden1 = createGarden();
    Garden garden2 = createGarden();

    eventRepo.save(
        WeatherEvent.builder()
            .gardenId(garden1.getId())
            .eventType("rain")
            .intensity(5)
            .occurredAt(LocalDateTime.now())
            .build());

    eventRepo.save(
        WeatherEvent.builder()
            .gardenId(garden1.getId())
            .eventType("drought")
            .intensity(3)
            .occurredAt(LocalDateTime.now())
            .build());

    eventRepo.save(
        WeatherEvent.builder()
            .gardenId(garden2.getId())
            .eventType("frost")
            .intensity(7)
            .occurredAt(LocalDateTime.now())
            .build());

    List<WeatherEvent> garden1Events = eventRepo.findByField("garden_id", garden1.getId());
    assertEquals(2, garden1Events.size());
  }

  @Test
  void shouldFindEventsByType() {
    WeatherEventRepository eventRepo = context.getWeatherEventRepository();
    Garden garden = createGarden();

    eventRepo.save(
        WeatherEvent.builder()
            .gardenId(garden.getId())
            .eventType("rain")
            .intensity(5)
            .occurredAt(LocalDateTime.now())
            .build());

    eventRepo.save(
        WeatherEvent.builder()
            .gardenId(garden.getId())
            .eventType("rain")
            .intensity(8)
            .occurredAt(LocalDateTime.now())
            .build());

    eventRepo.save(
        WeatherEvent.builder()
            .gardenId(garden.getId())
            .eventType("storm")
            .intensity(9)
            .occurredAt(LocalDateTime.now())
            .build());

    List<WeatherEvent> rainEvents = eventRepo.findByField("event_type", "rain");
    assertEquals(2, rainEvents.size());
  }

  @Test
  void shouldCountWeatherEvents() {
    WeatherEventRepository eventRepo = context.getWeatherEventRepository();
    Garden garden = createGarden();

    eventRepo.save(
        WeatherEvent.builder()
            .gardenId(garden.getId())
            .eventType("rain")
            .intensity(5)
            .occurredAt(LocalDateTime.now())
            .build());

    eventRepo.save(
        WeatherEvent.builder()
            .gardenId(garden.getId())
            .eventType("drought")
            .intensity(3)
            .occurredAt(LocalDateTime.now())
            .build());

    long count = eventRepo.count();
    assertEquals(2, count);
  }

  @Test
  void shouldHandleAllEventTypes() {
    WeatherEventRepository eventRepo = context.getWeatherEventRepository();
    Garden garden = createGarden();

    String[] eventTypes = {"rain", "drought", "frost", "heatwave", "storm"};

    for (String type : eventTypes) {
      WeatherEvent event =
          eventRepo.save(
              WeatherEvent.builder()
                  .gardenId(garden.getId())
                  .eventType(type)
                  .intensity(5)
                  .occurredAt(LocalDateTime.now())
                  .build());

      assertNotNull(event.getId());
      assertEquals(type, event.getEventType());
    }

    long count = eventRepo.count();
    assertEquals(5, count);
  }

  private Garden createGarden() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user" + System.nanoTime())
                .email("user" + System.nanoTime() + "@example.com")
                .createdAt(LocalDateTime.now())
                .build());

    return gardenRepo.save(
        Garden.builder()
            .userId(user.getId())
            .name("Garden")
            .widthCells(10)
            .heightCells(10)
            .createdAt(LocalDateTime.now())
            .build());
  }
}
