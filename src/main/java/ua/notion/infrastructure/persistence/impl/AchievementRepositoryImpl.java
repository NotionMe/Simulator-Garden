package ua.notion.infrastructure.persistence.impl;

import java.util.List;
import ua.notion.domain.entity.Achievement;
import ua.notion.infrastructure.persistence.GenericRepository;
import ua.notion.infrastructure.persistence.contract.AchievementRepository;
import ua.notion.infrastructure.websocket.WebSocketApiClient;

public class AchievementRepositoryImpl extends GenericRepository<Achievement, Integer>
    implements AchievementRepository {

  public AchievementRepositoryImpl(WebSocketApiClient apiClient) {
    super(apiClient, Achievement.class, "achievement");
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
    return findByUserId(userId).size();
  }
}
