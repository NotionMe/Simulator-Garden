package ua.notion.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import ua.notion.domain.entity.WeatherEvent;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.contract.WeatherEventRepository;

public class WeatherEventService {
  private final PersistenceContext context;
  private final WeatherEventRepository weatherEventRepository;

  public WeatherEventService(PersistenceContext context) {
    this.context = context;
    this.weatherEventRepository = context.getWeatherEventRepository();
  }

  public WeatherEvent createWeatherEvent(Integer gardenId, String eventType, Integer intensity) {
    if (intensity < 0 || intensity > 10) {
      throw new IllegalArgumentException("Intensity must be between 0 and 10");
    }

    WeatherEvent event =
        WeatherEvent.builder()
            .gardenId(gardenId)
            .eventType(eventType)
            .intensity(intensity)
            .occurredAt(LocalDateTime.now())
            .build();

    context.beginTransaction();
    try {
      weatherEventRepository.save(event);
      context.commitTransaction();
      return event;
    } catch (Exception e) {
      context.rollbackTransaction();
      throw new RuntimeException("Failed to create weather event", e);
    }
  }

  public Optional<WeatherEvent> findWeatherEventById(Integer id) {
    return weatherEventRepository.findById(id);
  }

  public List<WeatherEvent> findWeatherEventsByGardenId(Integer gardenId) {
    return weatherEventRepository.findAll().stream()
        .filter(we -> we.getGardenId().equals(gardenId))
        .toList();
  }

  public List<WeatherEvent> findWeatherEventsByType(String eventType) {
    return weatherEventRepository.findAll().stream()
        .filter(we -> we.getEventType().equalsIgnoreCase(eventType))
        .toList();
  }

  public List<WeatherEvent> findRecentWeatherEvents(Integer gardenId, int days) {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
    return weatherEventRepository.findAll().stream()
        .filter(we -> we.getGardenId().equals(gardenId) && we.getOccurredAt().isAfter(cutoff))
        .toList();
  }

  public Optional<WeatherEvent> findLatestWeatherEvent(Integer gardenId) {
    return weatherEventRepository.findAll().stream()
        .filter(we -> we.getGardenId().equals(gardenId))
        .max((a, b) -> a.getOccurredAt().compareTo(b.getOccurredAt()));
  }

  public void deleteWeatherEvent(Integer id) {
    context.beginTransaction();
    try {
      weatherEventRepository.delete(id);
      context.commitTransaction();
    } catch (Exception e) {
      context.rollbackTransaction();
      throw new RuntimeException("Failed to delete weather event", e);
    }
  }

  public void deleteOldWeatherEvents(Integer gardenId, int daysToKeep) {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
    List<WeatherEvent> oldEvents =
        weatherEventRepository.findAll().stream()
            .filter(we -> we.getGardenId().equals(gardenId) && we.getOccurredAt().isBefore(cutoff))
            .toList();

    context.beginTransaction();
    try {
      for (WeatherEvent event : oldEvents) {
        weatherEventRepository.delete(event.getId());
      }
      context.commitTransaction();
    } catch (Exception e) {
      context.rollbackTransaction();
      throw new RuntimeException("Failed to delete old weather events", e);
    }
  }
}
