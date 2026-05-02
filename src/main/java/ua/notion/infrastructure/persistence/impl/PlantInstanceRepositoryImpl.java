package ua.notion.infrastructure.persistence.impl;

import java.util.List;
import ua.notion.domain.entity.PlantInstance;
import ua.notion.infrastructure.persistence.GenericRepository;
import ua.notion.infrastructure.persistence.contract.PlantInstanceRepository;
import ua.notion.infrastructure.persistence.util.ConnectionPool;

public class PlantInstanceRepositoryImpl extends GenericRepository<PlantInstance, Integer>
    implements PlantInstanceRepository {

  public PlantInstanceRepositoryImpl(ConnectionPool connectionPool) {
    super(connectionPool, PlantInstance.class, "plant_instances");
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
    Filter filter =
        (whereClause, params) -> {
          whereClause.add("garden_id = ?");
          whereClause.add("cell_x = ?");
          whereClause.add("cell_y = ?");
          params.add(gardenId);
          params.add(cellX);
          params.add(cellY);
        };
    return findAll(filter, null, true, 0, Integer.MAX_VALUE);
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
