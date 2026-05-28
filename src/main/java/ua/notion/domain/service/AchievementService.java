package ua.notion.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import ua.notion.domain.entity.Achievement;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.contract.AchievementRepository;

public class AchievementService {
  private final PersistenceContext context;
  private final AchievementRepository achievementRepository;

  public AchievementService(PersistenceContext context) {
    this.context = context;
    this.achievementRepository = context.getAchievementRepository();
  }

  public Achievement unlockAchievement(Integer userId, String title, String conditionKey) {
    if (hasAchievement(userId, conditionKey)) {
      throw new IllegalStateException("Achievement already unlocked");
    }

    Achievement achievement =
        Achievement.builder()
            .userId(userId)
            .title(title)
            .conditionKey(conditionKey)
            .earnedAt(LocalDateTime.now())
            .build();

    try {
      return achievementRepository.save(achievement);
    } catch (Exception e) {
      throw new RuntimeException("Failed to unlock achievement", e);
    }
  }

  public Optional<Achievement> unlockIfMissing(Integer userId, String title, String conditionKey) {
    if (userId == null || hasAchievement(userId, conditionKey)) {
      return Optional.empty();
    }
    return Optional.of(unlockAchievement(userId, title, conditionKey));
  }

  public Optional<Achievement> findAchievementById(Integer id) {
    return achievementRepository.findById(id);
  }

  public List<Achievement> findAchievementsByUserId(Integer userId) {
    return achievementRepository.findAll().stream()
        .filter(a -> a.getUserId().equals(userId))
        .toList();
  }

  public boolean hasAchievement(Integer userId, String conditionKey) {
    return achievementRepository.findAll().stream()
        .anyMatch(a -> a.getUserId().equals(userId) && a.getConditionKey().equals(conditionKey));
  }

  public long countAchievements(Integer userId) {
    return achievementRepository.findAll().stream()
        .filter(a -> a.getUserId().equals(userId))
        .count();
  }

  public List<Achievement> findRecentAchievements(Integer userId, int days) {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
    return achievementRepository.findAll().stream()
        .filter(a -> a.getUserId().equals(userId) && a.getEarnedAt().isAfter(cutoff))
        .toList();
  }

  public void deleteAchievement(Integer id) {
    try {
      achievementRepository.delete(id);
    } catch (Exception e) {
      throw new RuntimeException("Failed to delete achievement", e);
    }
  }
}
