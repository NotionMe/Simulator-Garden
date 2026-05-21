package ua.notion.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.notion.domain.entity.Achievement;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.TestDatabaseManager;

class AchievementServiceTest {
  private TestDatabaseManager dbManager;
  private PersistenceContext context;
  private AchievementService achievementService;
  private UserService userService;

  private User testUser;

  @BeforeEach
  void setUp() {
    dbManager = new TestDatabaseManager();
    context = dbManager.createContext();
    achievementService = new AchievementService(context);
    userService = new UserService(context);

    testUser = userService.createUser("testuser", "test@example.com");
  }

  @AfterEach
  void tearDown() {
    if (context != null) {
      context.close();
    }
    dbManager.cleanup();
  }

  @Test
  void testUnlockAchievement() {
    Achievement achievement =
        achievementService.unlockAchievement(
            testUser.getId(), "First Garden", "first_garden_created");

    assertNotNull(achievement);
    assertNotNull(achievement.getId());
    assertEquals(testUser.getId(), achievement.getUserId());
    assertEquals("First Garden", achievement.getTitle());
    assertEquals("first_garden_created", achievement.getConditionKey());
    assertNotNull(achievement.getEarnedAt());
  }

  @Test
  void testUnlockAchievementAlreadyUnlocked() {
    achievementService.unlockAchievement(testUser.getId(), "First Garden", "first_garden_created");

    assertThrows(
        IllegalStateException.class,
        () ->
            achievementService.unlockAchievement(
                testUser.getId(), "First Garden", "first_garden_created"));
  }

  @Test
  void testFindAchievementById() {
    Achievement created =
        achievementService.unlockAchievement(
            testUser.getId(), "Plant Master", "planted_100_plants");

    Optional<Achievement> found = achievementService.findAchievementById(created.getId());

    assertTrue(found.isPresent());
    assertEquals("Plant Master", found.get().getTitle());
    assertEquals("planted_100_plants", found.get().getConditionKey());
  }

  @Test
  void testFindAchievementByIdNotFound() {
    Optional<Achievement> found = achievementService.findAchievementById(99999);
    assertFalse(found.isPresent());
  }

  @Test
  void testFindAchievementsByUserId() {
    achievementService.unlockAchievement(testUser.getId(), "First Garden", "first_garden");
    achievementService.unlockAchievement(testUser.getId(), "First Plant", "first_plant");
    achievementService.unlockAchievement(testUser.getId(), "First Harvest", "first_harvest");

    List<Achievement> achievements = achievementService.findAchievementsByUserId(testUser.getId());

    assertEquals(3, achievements.size());
    assertTrue(achievements.stream().allMatch(a -> a.getUserId().equals(testUser.getId())));
  }

  @Test
  void testHasAchievement() {
    assertFalse(achievementService.hasAchievement(testUser.getId(), "first_garden"));

    achievementService.unlockAchievement(testUser.getId(), "First Garden", "first_garden");

    assertTrue(achievementService.hasAchievement(testUser.getId(), "first_garden"));
  }

  @Test
  void testCountAchievements() {
    assertEquals(0, achievementService.countAchievements(testUser.getId()));

    achievementService.unlockAchievement(testUser.getId(), "Achievement 1", "ach1");
    achievementService.unlockAchievement(testUser.getId(), "Achievement 2", "ach2");
    achievementService.unlockAchievement(testUser.getId(), "Achievement 3", "ach3");

    assertEquals(3, achievementService.countAchievements(testUser.getId()));
  }

  @Test
  void testFindRecentAchievements() {
    achievementService.unlockAchievement(testUser.getId(), "Recent 1", "recent1");
    achievementService.unlockAchievement(testUser.getId(), "Recent 2", "recent2");

    Achievement old =
        Achievement.builder()
            .userId(testUser.getId())
            .title("Old Achievement")
            .conditionKey("old")
            .earnedAt(LocalDateTime.now().minusDays(10))
            .build();

    context.getAchievementRepository().save(old);

    List<Achievement> recent = achievementService.findRecentAchievements(testUser.getId(), 7);

    assertEquals(2, recent.size());
    assertTrue(recent.stream().noneMatch(a -> a.getConditionKey().equals("old")));
  }

  @Test
  void testDeleteAchievement() {
    Achievement achievement =
        achievementService.unlockAchievement(testUser.getId(), "To Delete", "delete_me");
    Integer achievementId = achievement.getId();

    achievementService.deleteAchievement(achievementId);

    Optional<Achievement> deleted = achievementService.findAchievementById(achievementId);
    assertFalse(deleted.isPresent());
  }

  @Test
  void testMultipleUsersAchievements() {
    User user2 = userService.createUser("user2", "user2@example.com");

    achievementService.unlockAchievement(testUser.getId(), "User1 Ach", "user1_ach");
    achievementService.unlockAchievement(user2.getId(), "User2 Ach", "user2_ach");

    List<Achievement> user1Achievements =
        achievementService.findAchievementsByUserId(testUser.getId());
    List<Achievement> user2Achievements =
        achievementService.findAchievementsByUserId(user2.getId());

    assertEquals(1, user1Achievements.size());
    assertEquals(1, user2Achievements.size());
    assertNotEquals(
        user1Achievements.get(0).getConditionKey(), user2Achievements.get(0).getConditionKey());
  }
}
