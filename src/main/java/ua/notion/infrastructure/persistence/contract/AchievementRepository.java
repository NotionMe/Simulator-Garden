package ua.notion.infrastructure.persistence.contract;

import java.util.List;
import ua.notion.domain.entity.Achievement;
import ua.notion.infrastructure.persistence.Repository;

public interface AchievementRepository extends Repository<Achievement, Integer> {

  List<Achievement> findByUserId(Integer userId);

  List<Achievement> findByConditionKey(String conditionKey);

  long countByUserId(Integer userId);
}
