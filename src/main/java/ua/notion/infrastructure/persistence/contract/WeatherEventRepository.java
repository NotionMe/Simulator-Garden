package ua.notion.infrastructure.persistence.contract;

import java.util.List;
import ua.notion.domain.entity.WeatherEvent;
import ua.notion.infrastructure.persistence.Repository;

public interface WeatherEventRepository extends Repository<WeatherEvent, Integer> {

  List<WeatherEvent> findByGardenId(Integer gardenId);

  List<WeatherEvent> findByEventType(String eventType);

  long countByGardenId(Integer gardenId);
}
