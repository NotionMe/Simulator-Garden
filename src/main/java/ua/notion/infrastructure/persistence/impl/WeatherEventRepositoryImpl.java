package ua.notion.infrastructure.persistence.impl;

import java.util.List;
import ua.notion.domain.entity.WeatherEvent;
import ua.notion.infrastructure.persistence.GenericRepository;
import ua.notion.infrastructure.persistence.contract.WeatherEventRepository;
import ua.notion.infrastructure.websocket.WebSocketApiClient;

public class WeatherEventRepositoryImpl extends GenericRepository<WeatherEvent, Integer>
    implements WeatherEventRepository {

  public WeatherEventRepositoryImpl(WebSocketApiClient apiClient) {
    super(apiClient, WeatherEvent.class, "weatherevent");
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
    return findByGardenId(gardenId).size();
  }
}
