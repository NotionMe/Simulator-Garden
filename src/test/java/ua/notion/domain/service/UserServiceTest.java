package ua.notion.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.TestDatabaseManager;

class UserServiceTest {
  private TestDatabaseManager testDbManager;
  private PersistenceContext context;
  private UserService userService;

  @BeforeEach
  void setUp() throws Exception {
    testDbManager = new TestDatabaseManager();
    testDbManager.setup();
    context = new PersistenceContext(testDbManager.getConnectionPool());
    userService = new UserService(context);
  }

  @AfterEach
  void tearDown() {
    if (context != null) {
      context.close();
    }
    testDbManager.teardown();
  }

  @Test
  void testCreateUser() {
    User user = userService.createUser("testuser", "test@example.com");

    assertNotNull(user);
    assertNotNull(user.getId());
    assertEquals("testuser", user.getUsername());
    assertEquals("test@example.com", user.getEmail());
    assertNotNull(user.getCreatedAt());
  }

  @Test
  void testFindUserById() {
    User created = userService.createUser("testuser", "test@example.com");

    Optional<User> found = userService.findUserById(created.getId());

    assertTrue(found.isPresent());
    assertEquals(created.getId(), found.get().getId());
    assertEquals("testuser", found.get().getUsername());
  }

  @Test
  void testFindUserByUsername() {
    userService.createUser("testuser", "test@example.com");

    Optional<User> found = userService.findUserByUsername("testuser");

    assertTrue(found.isPresent());
    assertEquals("testuser", found.get().getUsername());
  }

  @Test
  void testFindUserByEmail() {
    userService.createUser("testuser", "test@example.com");

    Optional<User> found = userService.findUserByEmail("test@example.com");

    assertTrue(found.isPresent());
    assertEquals("test@example.com", found.get().getEmail());
  }

  @Test
  void testGetAllUsers() {
    userService.createUser("user1", "user1@example.com");
    userService.createUser("user2", "user2@example.com");

    List<User> users = userService.getAllUsers();

    assertEquals(2, users.size());
  }

  @Test
  void testUpdateUser() {
    User user = userService.createUser("testuser", "test@example.com");
    user.setUsername("updateduser");
    user.setEmail("updated@example.com");

    userService.updateUser(user);

    Optional<User> updated = userService.findUserById(user.getId());
    assertTrue(updated.isPresent());
    assertEquals("updateduser", updated.get().getUsername());
    assertEquals("updated@example.com", updated.get().getEmail());
  }

  @Test
  void testUpdateUserWithNullId() {
    User user = User.builder().username("test").email("test@example.com").build();

    assertThrows(IllegalArgumentException.class, () -> userService.updateUser(user));
  }

  @Test
  void testDeleteUser() {
    User user = userService.createUser("testuser", "test@example.com");
    Integer userId = user.getId();

    userService.deleteUser(userId);

    Optional<User> deleted = userService.findUserById(userId);
    assertFalse(deleted.isPresent());
  }
}
