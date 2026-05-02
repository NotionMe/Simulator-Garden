package ua.notion.infrastructure.persistence.impl;

import java.util.List;
import ua.notion.domain.entity.WeatherEvent;
import ua.notion.infrastructure.persistence.GenericRepository;
import ua.notion.infrastructure.persistence.contract.WeatherEventRepository;
import ua.notion.infrastructure.persistence.util.ConnectionPool;

public class WeatherEventRepositoryImpl extends GenericRepository<WeatherEvent, Integer>
    implements WeatherEventRepository {

  public WeatherEventRepositoryImpl(ConnectionPool connectionPool) {
    super(connectionPool, WeatherEvent.class, "weather_events");
  }

  @Override
  public List<WeatherEvent> findByGardenId(Integer gardenId) {
    return findByField("garden_id", gardenId);
  }

  @Override
  public List<WeatherEvent> findByEventType(String eventType) {
    return findByField("event_type", eventType);
  }

  @Override
  public long countByGardenId(Integer gardenId) {
    Filter filter =
        (whereClause, params) -> {
          whereClause.add("garden_id = ?");
          params.add(gardenId);
        };
    return count(filter);
  }
}
