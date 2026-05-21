package ua.notion.infrastructure.persistence.impl;

import java.util.List;
import ua.notion.domain.entity.Plant;
import ua.notion.infrastructure.persistence.GenericRepository;
import ua.notion.infrastructure.persistence.contract.PlantRepository;
import ua.notion.infrastructure.websocket.WebSocketApiClient;

public class PlantRepositoryImpl extends GenericRepository<Plant, Integer>
    implements PlantRepository {

  public PlantRepositoryImpl(WebSocketApiClient apiClient) {
    super(apiClient, Plant.class, "plant");
  }

  @Override
  public List<Plant> findByClimateType(String climateType) {
    return findByField("climate_type", climateType);
  }
}
