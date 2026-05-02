package ua.notion.infrastructure.persistence.contract;

import java.util.List;
import ua.notion.domain.entity.Plant;
import ua.notion.infrastructure.persistence.Repository;

public interface PlantRepository extends Repository<Plant, Integer> {

  List<Plant> findByClimateType(String climateType);
}
