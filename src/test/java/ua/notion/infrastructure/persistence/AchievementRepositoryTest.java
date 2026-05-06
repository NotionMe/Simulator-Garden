package ua.notion.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import ua.notion.domain.entity.Achievement;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.contract.AchievementRepository;
import ua.notion.infrastructure.persistence.contract.UserRepository;

class AchievementRepositoryTest extends BaseRepositoryTest {

  @Test
  void shouldSaveAndFindAchievementById() {
    UserRepository userRepo = context.getUserRepository();
    AchievementRepository achievementRepo = context.getAchievementRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
                .createdAt(LocalDateTime.now())
                .build());

    Achievement achievement =
        Achievement.builder()
            .userId(user.getId())
            .title("First Garden")
            .conditionKey("garden_created")
            .earnedAt(LocalDateTime.now())
            .build();

    Achievement saved = achievementRepo.save(achievement);

    assertNotNull(saved.getId());
    assertEquals("First Garden", saved.getTitle());
    assertEquals("garden_created", saved.getConditionKey());

    Optional<Achievement> found = achievementRepo.findById(saved.getId());
    assertTrue(found.isPresent());
    assertEquals("First Garden", found.get().getTitle());
  }

  @Test
  void shouldUpdateAchievement() {
    AchievementRepository achievementRepo = context.getAchievementRepository();
    User user = createUser();

    Achievement achievement =
        achievementRepo.save(
            Achievement.builder()
                .userId(user.getId())
                .title("Original Title")
                .conditionKey("condition_1")
                .earnedAt(LocalDateTime.now())
                .build());

    achievement.setTitle("Updated Title");

    achievementRepo.update(achievement.getId(), achievement);

    Optional<Achievement> updated = achievementRepo.findById(achievement.getId());
    assertTrue(updated.isPresent());
    assertEquals("Updated Title", updated.get().getTitle());
  }

  @Test
  void shouldDeleteAchievement() {
    AchievementRepository achievementRepo = context.getAchievementRepository();
    User user = createUser();

    Achievement achievement =
        achievementRepo.save(
            Achievement.builder()
                .userId(user.getId())
                .title("To Delete")
                .conditionKey("delete_me")
                .earnedAt(LocalDateTime.now())
                .build());

    Integer id = achievement.getId();
    achievementRepo.delete(id);

    Optional<Achievement> deleted = achievementRepo.findById(id);
    assertFalse(deleted.isPresent());
  }

  @Test
  void shouldEnforceUniqueUserConditionKey() {
    AchievementRepository achievementRepo = context.getAchievementRepository();
    User user = createUser();

    achievementRepo.save(
        Achievement.builder()
            .userId(user.getId())
            .title("Achievement 1")
            .conditionKey("unique_condition")
            .earnedAt(LocalDateTime.now())
            .build());

    assertThrows(
        Exception.class,
        () ->
            achievementRepo.save(
                Achievement.builder()
                    .userId(user.getId())
                    .title("Achievement 2")
                    .conditionKey("unique_condition")
                    .earnedAt(LocalDateTime.now())
                    .build()));
  }

  @Test
  void shouldAllowSameConditionKeyForDifferentUsers() {
    AchievementRepository achievementRepo = context.getAchievementRepository();
    User user1 = createUser();
    User user2 = createUser();

    Achievement achievement1 =
        achievementRepo.save(
            Achievement.builder()
                .userId(user1.getId())
                .title("Achievement 1")
                .conditionKey("same_condition")
                .earnedAt(LocalDateTime.now())
                .build());

    Achievement achievement2 =
        achievementRepo.save(
            Achievement.builder()
                .userId(user2.getId())
                .title("Achievement 2")
                .conditionKey("same_condition")
                .earnedAt(LocalDateTime.now())
                .build());

    assertNotNull(achievement1.getId());
    assertNotNull(achievement2.getId());
    assertNotEquals(achievement1.getId(), achievement2.getId());
  }

  @Test
  void shouldCascadeDeleteWhenUserDeleted() {
    UserRepository userRepo = context.getUserRepository();
    AchievementRepository achievementRepo = context.getAchievementRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
                .createdAt(LocalDateTime.now())
                .build());

    Achievement achievement =
        achievementRepo.save(
            Achievement.builder()
                .userId(user.getId())
                .title("Achievement")
                .conditionKey("condition")
                .earnedAt(LocalDateTime.now())
                .build());

    Integer achievementId = achievement.getId();
    userRepo.delete(user.getId());

    Optional<Achievement> deleted = achievementRepo.findById(achievementId);
    assertFalse(deleted.isPresent());
  }

  @Test
  void shouldFindAchievementsByUserId() {
    AchievementRepository achievementRepo = context.getAchievementRepository();
    User user1 = createUser();
    User user2 = createUser();

    achievementRepo.save(
        Achievement.builder()
            .userId(user1.getId())
            .title("Achievement 1")
            .conditionKey("condition_1")
            .earnedAt(LocalDateTime.now())
            .build());

    achievementRepo.save(
        Achievement.builder()
            .userId(user1.getId())
            .title("Achievement 2")
            .conditionKey("condition_2")
            .earnedAt(LocalDateTime.now())
            .build());

    achievementRepo.save(
        Achievement.builder()
            .userId(user2.getId())
            .title("Achievement 3")
            .conditionKey("condition_3")
            .earnedAt(LocalDateTime.now())
            .build());

    List<Achievement> user1Achievements = achievementRepo.findByField("user_id", user1.getId());
    assertEquals(2, user1Achievements.size());
  }

  @Test
  void shouldFindAchievementsByConditionKey() {
    AchievementRepository achievementRepo = context.getAchievementRepository();
    User user1 = createUser();
    User user2 = createUser();

    achievementRepo.save(
        Achievement.builder()
            .userId(user1.getId())
            .title("Achievement 1")
            .conditionKey("first_plant")
            .earnedAt(LocalDateTime.now())
            .build());

    achievementRepo.save(
        Achievement.builder()
            .userId(user2.getId())
            .title("Achievement 2")
            .conditionKey("first_plant")
            .earnedAt(LocalDateTime.now())
            .build());

    achievementRepo.save(
        Achievement.builder()
            .userId(user1.getId())
            .title("Achievement 3")
            .conditionKey("first_harvest")
            .earnedAt(LocalDateTime.now())
            .build());

    List<Achievement> firstPlantAchievements =
        achievementRepo.findByField("condition_key", "first_plant");
    assertEquals(2, firstPlantAchievements.size());
  }

  @Test
  void shouldCountAchievements() {
    AchievementRepository achievementRepo = context.getAchievementRepository();
    User user = createUser();

    achievementRepo.save(
        Achievement.builder()
            .userId(user.getId())
            .title("Achievement 1")
            .conditionKey("condition_1")
            .earnedAt(LocalDateTime.now())
            .build());

    achievementRepo.save(
        Achievement.builder()
            .userId(user.getId())
            .title("Achievement 2")
            .conditionKey("condition_2")
            .earnedAt(LocalDateTime.now())
            .build());

    long count = achievementRepo.count();
    assertEquals(2, count);
  }

  @Test
  void shouldFindAllAchievements() {
    AchievementRepository achievementRepo = context.getAchievementRepository();
    User user = createUser();

    achievementRepo.save(
        Achievement.builder()
            .userId(user.getId())
            .title("Achievement 1")
            .conditionKey("condition_1")
            .earnedAt(LocalDateTime.now())
            .build());

    achievementRepo.save(
        Achievement.builder()
            .userId(user.getId())
            .title("Achievement 2")
            .conditionKey("condition_2")
            .earnedAt(LocalDateTime.now())
            .build());

    List<Achievement> achievements = achievementRepo.findAll();
    assertEquals(2, achievements.size());
  }

  private User createUser() {
    UserRepository userRepo = context.getUserRepository();
    return userRepo.save(
        User.builder()
            .username("user" + System.nanoTime())
            .email("user" + System.nanoTime() + "@example.com")
            .createdAt(LocalDateTime.now())
            .build());
  }
}
