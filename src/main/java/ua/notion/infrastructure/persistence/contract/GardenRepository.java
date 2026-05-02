package ua.notion.infrastructure.persistence.contract;

import java.util.List;
import ua.notion.domain.entity.Garden;
import ua.notion.infrastructure.persistence.Repository;

public interface GardenRepository extends Repository<Garden, Integer> {

  List<Garden> findByUserId(Integer userId);

  long countByUserId(Integer userId);
}
