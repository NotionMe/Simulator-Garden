package ua.notion.infrastructure.persistence.impl;

import java.util.List;
import ua.notion.domain.entity.Garden;
import ua.notion.infrastructure.persistence.GenericRepository;
import ua.notion.infrastructure.persistence.contract.GardenRepository;
import ua.notion.infrastructure.websocket.WebSocketApiClient;

public class GardenRepositoryImpl extends GenericRepository<Garden, Integer>
    implements GardenRepository {

  public GardenRepositoryImpl(WebSocketApiClient apiClient) {
    super(apiClient, Garden.class, "garden");
  }

  @Override
  public List<Garden> findByUserId(Integer userId) {
    return findByField("user_id", userId);
  }

  @Override
  public long countByUserId(Integer userId) {
    return findByUserId(userId).size();
  }
}
