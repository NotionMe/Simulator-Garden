package ua.notion.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.contract.UserRepository;

class UserRepositoryTest extends BaseRepositoryTest {

  @Test
  void shouldSaveAndFindUserById() {
    UserRepository repo = context.getUserRepository();

    User user =
        User.builder()
            .username("testuser")
            .email("test@example.com")
            .createdAt(LocalDateTime.now())
            .build();

    User saved = repo.save(user);

    assertNotNull(saved.getId());
    assertEquals("testuser", saved.getUsername());

    Optional<User> found = repo.findById(saved.getId());
    assertTrue(found.isPresent());
    assertEquals("testuser", found.get().getUsername());
    assertEquals("test@example.com", found.get().getEmail());
  }

  @Test
  void shouldUpdateUser() {
    UserRepository repo = context.getUserRepository();

    User user =
        User.builder()
            .username("original")
            .email("original@example.com")
            .createdAt(LocalDateTime.now())
            .build();

    User saved = repo.save(user);
    saved.setEmail("updated@example.com");

    repo.update(saved.getId(), saved);

    Optional<User> updated = repo.findById(saved.getId());
    assertTrue(updated.isPresent());
    assertEquals("updated@example.com", updated.get().getEmail());
  }

  @Test
  void shouldDeleteUser() {
    UserRepository repo = context.getUserRepository();

    User user =
        User.builder()
            .username("todelete")
            .email("delete@example.com")
            .createdAt(LocalDateTime.now())
            .build();

    User saved = repo.save(user);
    Integer id = saved.getId();

    repo.delete(id);

    Optional<User> deleted = repo.findById(id);
    assertFalse(deleted.isPresent());
  }

  @Test
  void shouldFindAllUsers() {
    UserRepository repo = context.getUserRepository();

    repo.save(
        User.builder()
            .username("user1")
            .email("user1@example.com")
            .createdAt(LocalDateTime.now())
            .build());
    repo.save(
        User.builder()
            .username("user2")
            .email("user2@example.com")
            .createdAt(LocalDateTime.now())
            .build());

    List<User> users = repo.findAll();
    assertEquals(2, users.size());
  }

  @Test
  void shouldEnforceUniqueUsername() {
    UserRepository repo = context.getUserRepository();

    repo.save(
        User.builder()
            .username("unique")
            .email("first@example.com")
            .createdAt(LocalDateTime.now())
            .build());

    assertThrows(
        Exception.class,
        () ->
            repo.save(
                User.builder()
                    .username("unique")
                    .email("second@example.com")
                    .createdAt(LocalDateTime.now())
                    .build()));
  }

  @Test
  void shouldEnforceUniqueEmail() {
    UserRepository repo = context.getUserRepository();

    repo.save(
        User.builder()
            .username("user1")
            .email("same@example.com")
            .createdAt(LocalDateTime.now())
            .build());

    assertThrows(
        Exception.class,
        () ->
            repo.save(
                User.builder()
                    .username("user2")
                    .email("same@example.com")
                    .createdAt(LocalDateTime.now())
                    .build()));
  }

  @Test
  void shouldFilterUsersByUsername() {
    UserRepository repo = context.getUserRepository();

    repo.save(
        User.builder()
            .username("alice")
            .email("alice@example.com")
            .createdAt(LocalDateTime.now())
            .build());
    repo.save(
        User.builder()
            .username("bob")
            .email("bob@example.com")
            .createdAt(LocalDateTime.now())
            .build());

    List<User> filtered = repo.findByField("username", "alice");
    assertEquals(1, filtered.size());
    assertEquals("alice", filtered.get(0).getUsername());
  }

  @Test
  void shouldHandlePagination() {
    UserRepository repo = context.getUserRepository();

    for (int i = 1; i <= 5; i++) {
      repo.save(
          User.builder()
              .username("user" + i)
              .email("user" + i + "@example.com")
              .createdAt(LocalDateTime.now())
              .build());
    }

    List<User> page1 = repo.findAll(0, 2);
    assertEquals(2, page1.size());

    List<User> page2 = repo.findAll(2, 2);
    assertEquals(2, page2.size());
    assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
  }

  @Test
  void shouldCountUsers() {
    UserRepository repo = context.getUserRepository();

    repo.save(
        User.builder()
            .username("user1")
            .email("user1@example.com")
            .createdAt(LocalDateTime.now())
            .build());
    repo.save(
        User.builder()
            .username("user2")
            .email("user2@example.com")
            .createdAt(LocalDateTime.now())
            .build());

    long count = repo.count();
    assertEquals(2, count);
  }
}
