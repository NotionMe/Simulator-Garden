package ua.notion.infrastructure.persistence.contract;

import java.util.List;
import ua.notion.domain.entity.PlantInstance;
import ua.notion.infrastructure.persistence.Repository;

public interface PlantInstanceRepository extends Repository<PlantInstance, Integer> {

  List<PlantInstance> findByGardenId(Integer gardenId);

  List<PlantInstance> findByPlantId(Integer plantId);

  List<PlantInstance> findByGardenIdAndCellPosition(Integer gardenId, Integer cellX, Integer cellY);

  long countByGardenId(Integer gardenId);
}
