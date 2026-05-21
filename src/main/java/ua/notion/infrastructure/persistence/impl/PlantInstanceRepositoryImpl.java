package ua.notion.infrastructure.persistence.impl;

import java.util.List;
import ua.notion.domain.entity.PlantInstance;
import ua.notion.infrastructure.persistence.GenericRepository;
import ua.notion.infrastructure.persistence.contract.PlantInstanceRepository;
import ua.notion.infrastructure.websocket.WebSocketApiClient;

public class PlantInstanceRepositoryImpl extends GenericRepository<PlantInstance, Integer>
    implements PlantInstanceRepository {

  public PlantInstanceRepositoryImpl(WebSocketApiClient apiClient) {
    super(apiClient, PlantInstance.class, "plantinstance");
  }

  @Override
  public List<PlantInstance> findByGardenId(Integer gardenId) {
    return findByField("garden_id", gardenId);
  }

  @Override
  public List<PlantInstance> findByPlantId(Integer plantId) {
    return findByField("plant_id", plantId);
  }

  @Override
  public List<PlantInstance> findByGardenIdAndCellPosition(
      Integer gardenId, Integer cellX, Integer cellY) {
    return findByGardenId(gardenId).stream()
        .filter(pi -> pi.getCellX().equals(cellX) && pi.getCellY().equals(cellY))
        .toList();
  }

  @Override
  public long countByGardenId(Integer gardenId) {
    return findByGardenId(gardenId).size();
  }
}
