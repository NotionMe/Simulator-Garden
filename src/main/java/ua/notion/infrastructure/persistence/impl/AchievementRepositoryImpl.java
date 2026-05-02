package ua.notion.infrastructure.persistence.impl;

import java.util.List;
import ua.notion.domain.entity.Achievement;
import ua.notion.infrastructure.persistence.GenericRepository;
import ua.notion.infrastructure.persistence.contract.AchievementRepository;
import ua.notion.infrastructure.persistence.util.ConnectionPool;

public class AchievementRepositoryImpl extends GenericRepository<Achievement, Integer>
    implements AchievementRepository {

  public AchievementRepositoryImpl(ConnectionPool connectionPool) {
    super(connectionPool, Achievement.class, "achievements");
  }

  @Override
  public List<Achievement> findByUserId(Integer userId) {
    return findByField("user_id", userId);
  }

  @Override
  public List<Achievement> findByConditionKey(String conditionKey) {
    return findByField("condition_key", conditionKey);
  }

  @Override
  public long countByUserId(Integer userId) {
    Filter filter =
        (whereClause, params) -> {
          whereClause.add("user_id = ?");
          params.add(userId);
        };
    return count(filter);
  }
}
