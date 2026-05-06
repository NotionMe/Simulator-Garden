package ua.notion.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import ua.notion.domain.entity.Garden;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.contract.GardenRepository;
import ua.notion.infrastructure.persistence.contract.UserRepository;

class GardenRepositoryTest extends BaseRepositoryTest {

  @Test
  void shouldSaveAndFindGardenById() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("gardener")
                .email("gardener@example.com")
                .createdAt(LocalDateTime.now())
                .build());

    Garden garden =
        Garden.builder()
            .userId(user.getId())
            .name("My Garden")
            .widthCells(10)
            .heightCells(10)
            .createdAt(LocalDateTime.now())
            .build();

    Garden saved = gardenRepo.save(garden);

    assertNotNull(saved.getId());
    assertEquals("My Garden", saved.getName());
    assertEquals(10, saved.getWidthCells());

    Optional<Garden> found = gardenRepo.findById(saved.getId());
    assertTrue(found.isPresent());
    assertEquals("My Garden", found.get().getName());
  }

  @Test
  void shouldUpdateGarden() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
                .createdAt(LocalDateTime.now())
                .build());

    Garden garden =
        gardenRepo.save(
            Garden.builder()
                .userId(user.getId())
                .name("Original")
                .widthCells(5)
                .heightCells(5)
                .createdAt(LocalDateTime.now())
                .build());

    garden.setName("Updated Garden");
    garden.setWidthCells(15);

    gardenRepo.update(garden.getId(), garden);

    Optional<Garden> updated = gardenRepo.findById(garden.getId());
    assertTrue(updated.isPresent());
    assertEquals("Updated Garden", updated.get().getName());
    assertEquals(15, updated.get().getWidthCells());
  }

  @Test
  void shouldDeleteGarden() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
                .createdAt(LocalDateTime.now())
                .build());

    Garden garden =
        gardenRepo.save(
            Garden.builder()
                .userId(user.getId())
                .name("ToDelete")
                .widthCells(5)
                .heightCells(5)
                .createdAt(LocalDateTime.now())
                .build());

    Integer id = garden.getId();
    gardenRepo.delete(id);

    Optional<Garden> deleted = gardenRepo.findById(id);
    assertFalse(deleted.isPresent());
  }

  @Test
  void shouldCascadeDeleteWhenUserDeleted() throws Exception {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
                .createdAt(LocalDateTime.now())
                .build());

    Garden garden =
        gardenRepo.save(
            Garden.builder()
                .userId(user.getId())
                .name("Garden")
                .widthCells(5)
                .heightCells(5)
                .createdAt(LocalDateTime.now())
                .build());

    Integer gardenId = garden.getId();
    userRepo.delete(user.getId());

    Optional<Garden> deleted = gardenRepo.findById(gardenId);
    assertFalse(deleted.isPresent());
  }

  @Test
  void shouldFindGardensByUserId() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();

    User user1 =
        userRepo.save(
            User.builder()
                .username("user1")
                .email("user1@example.com")
                .createdAt(LocalDateTime.now())
                .build());
    User user2 =
        userRepo.save(
            User.builder()
                .username("user2")
                .email("user2@example.com")
                .createdAt(LocalDateTime.now())
                .build());

    gardenRepo.save(
        Garden.builder()
            .userId(user1.getId())
            .name("Garden1")
            .widthCells(5)
            .heightCells(5)
            .createdAt(LocalDateTime.now())
            .build());
    gardenRepo.save(
        Garden.builder()
            .userId(user1.getId())
            .name("Garden2")
            .widthCells(5)
            .heightCells(5)
            .createdAt(LocalDateTime.now())
            .build());
    gardenRepo.save(
        Garden.builder()
            .userId(user2.getId())
            .name("Garden3")
            .widthCells(5)
            .heightCells(5)
            .createdAt(LocalDateTime.now())
            .build());

    List<Garden> user1Gardens = gardenRepo.findByField("user_id", user1.getId());
    assertEquals(2, user1Gardens.size());
  }

  @Test
  void shouldEnforceSizeConstraints() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
                .createdAt(LocalDateTime.now())
                .build());

    assertThrows(
        Exception.class,
        () ->
            gardenRepo.save(
                Garden.builder()
                    .userId(user.getId())
                    .name("Invalid")
                    .widthCells(0)
                    .heightCells(5)
                    .createdAt(LocalDateTime.now())
                    .build()));

    assertThrows(
        Exception.class,
        () ->
            gardenRepo.save(
                Garden.builder()
                    .userId(user.getId())
                    .name("Invalid")
                    .widthCells(101)
                    .heightCells(5)
                    .createdAt(LocalDateTime.now())
                    .build()));
  }

  @Test
  void shouldCountGardens() {
    UserRepository userRepo = context.getUserRepository();
    GardenRepository gardenRepo = context.getGardenRepository();

    User user =
        userRepo.save(
            User.builder()
                .username("user")
                .email("user@example.com")
                .createdAt(LocalDateTime.now())
                .build());

    gardenRepo.save(
        Garden.builder()
            .userId(user.getId())
            .name("Garden1")
            .widthCells(5)
            .heightCells(5)
            .createdAt(LocalDateTime.now())
            .build());
    gardenRepo.save(
        Garden.builder()
            .userId(user.getId())
            .name("Garden2")
            .widthCells(5)
            .heightCells(5)
            .createdAt(LocalDateTime.now())
            .build());

    long count = gardenRepo.count();
    assertEquals(2, count);
  }
}
