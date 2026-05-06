package ua.notion.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.contract.UserRepository;

class UnitOfWorkTest extends BaseRepositoryTest {

  @Test
  void shouldBeginTransaction() {
    UnitOfWork uow = context.beginTransaction();

    assertTrue(uow.isActive());
    assertNotNull(uow.getConnection());
  }

  @Test
  void shouldCommitTransaction() throws Exception {
    UserRepository userRepo = context.getUserRepository();
    UnitOfWork uow = context.beginTransaction();

    User user =
        User.builder()
            .username("testuser")
            .email("test@example.com")
            .createdAt(LocalDateTime.now())
            .build();

    userRepo.save(user);
    context.commitTransaction();

    assertFalse(uow.isActive());
    assertTrue(userRepo.findById(user.getId()).isPresent());
  }

  @Test
  void shouldRollbackTransaction() {
    UserRepository userRepo = context.getUserRepository();
    UnitOfWork uow = context.beginTransaction();

    User user =
        User.builder()
            .username("testuser")
            .email("test@example.com")
            .createdAt(LocalDateTime.now())
            .build();

    User saved = userRepo.save(user);
    Integer userId = saved.getId();

    context.rollbackTransaction();

    assertFalse(uow.isActive());
    assertFalse(userRepo.findById(userId).isPresent());
  }

  @Test
  void shouldRegisterNewEntity() {
    UnitOfWork uow = context.beginTransaction();

    User user = User.builder().username("newuser").email("new@example.com").build();

    uow.registerNew(user);

    assertTrue(uow.getNewEntities().contains(user));
    assertFalse(uow.getDirtyEntities().contains(user));
    assertFalse(uow.getRemovedEntities().contains(user));
  }

  @Test
  void shouldRegisterDirtyEntity() {
    UnitOfWork uow = context.beginTransaction();

    User user = User.builder().id(1).username("existinguser").email("existing@example.com").build();

    uow.registerDirty(user);

    assertTrue(uow.getDirtyEntities().contains(user));
    assertFalse(uow.getNewEntities().contains(user));
    assertFalse(uow.getRemovedEntities().contains(user));
  }

  @Test
  void shouldRegisterRemovedEntity() {
    UnitOfWork uow = context.beginTransaction();

    User user = User.builder().id(1).username("toremove").email("remove@example.com").build();

    uow.registerRemoved(user);

    assertTrue(uow.getRemovedEntities().contains(user));
    assertFalse(uow.getNewEntities().contains(user));
    assertFalse(uow.getDirtyEntities().contains(user));
  }

  @Test
  void shouldRemoveNewEntityWhenRegisteredAsRemoved() {
    UnitOfWork uow = context.beginTransaction();

    User user = User.builder().username("newuser").email("new@example.com").build();

    uow.registerNew(user);
    assertTrue(uow.getNewEntities().contains(user));

    uow.registerRemoved(user);

    assertFalse(uow.getNewEntities().contains(user));
    assertFalse(uow.getRemovedEntities().contains(user));
  }

  @Test
  void shouldNotRegisterDirtyIfAlreadyNew() {
    UnitOfWork uow = context.beginTransaction();

    User user = User.builder().username("newuser").email("new@example.com").build();

    uow.registerNew(user);
    uow.registerDirty(user);

    assertTrue(uow.getNewEntities().contains(user));
    assertFalse(uow.getDirtyEntities().contains(user));
  }

  @Test
  void shouldThrowExceptionWhenRegisteringRemovedAsNew() {
    UnitOfWork uow = context.beginTransaction();

    User user = User.builder().id(1).username("user").email("user@example.com").build();

    uow.registerRemoved(user);

    assertThrows(IllegalStateException.class, () -> uow.registerNew(user));
  }

  @Test
  void shouldClearEntitiesAfterCommit() {
    UserRepository userRepo = context.getUserRepository();
    UnitOfWork uow = context.beginTransaction();

    User user =
        User.builder()
            .username("testuser")
            .email("test@example.com")
            .createdAt(LocalDateTime.now())
            .build();

    uow.registerNew(user);
    userRepo.save(user);

    assertTrue(uow.getNewEntities().contains(user));

    context.commitTransaction();

    assertTrue(uow.getNewEntities().isEmpty());
    assertTrue(uow.getDirtyEntities().isEmpty());
    assertTrue(uow.getRemovedEntities().isEmpty());
  }

  @Test
  void shouldClearEntitiesAfterRollback() {
    UnitOfWork uow = context.beginTransaction();

    User user = User.builder().username("testuser").email("test@example.com").build();

    uow.registerNew(user);
    assertTrue(uow.getNewEntities().contains(user));

    context.rollbackTransaction();

    assertTrue(uow.getNewEntities().isEmpty());
    assertTrue(uow.getDirtyEntities().isEmpty());
    assertTrue(uow.getRemovedEntities().isEmpty());
  }

  @Test
  void shouldThrowExceptionWhenCommittingWithoutActiveTransaction() {
    assertThrows(IllegalStateException.class, () -> context.commitTransaction());
  }

  @Test
  void shouldThrowExceptionWhenBeginningTransactionTwice() {
    context.beginTransaction();

    assertThrows(IllegalStateException.class, () -> context.beginTransaction());
  }

  @Test
  void shouldGetConnectionOnlyWhenTransactionActive() {
    UnitOfWork uow = new UnitOfWork(dbManager.getConnectionPool());

    assertThrows(IllegalStateException.class, uow::getConnection);

    uow.begin();
    assertDoesNotThrow(uow::getConnection);
  }

  @Test
  void shouldHandleMultipleRepositoriesInSameTransaction() {
    UserRepository userRepo = context.getUserRepository();
    UnitOfWork uow = context.beginTransaction();

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

    context.commitTransaction();

    assertTrue(userRepo.findById(user1.getId()).isPresent());
    assertTrue(userRepo.findById(user2.getId()).isPresent());
  }

  @Test
  void shouldRollbackAllChangesOnError() {
    UserRepository userRepo = context.getUserRepository();
    UnitOfWork uow = context.beginTransaction();

    User user1 =
        userRepo.save(
            User.builder()
                .username("user1")
                .email("user1@example.com")
                .createdAt(LocalDateTime.now())
                .build());

    Integer userId1 = user1.getId();

    try {
      userRepo.save(
          User.builder()
              .username("user1")
              .email("duplicate@example.com")
              .createdAt(LocalDateTime.now())
              .build());
      context.commitTransaction();
      fail("Should have thrown exception");
    } catch (Exception e) {
      context.rollbackTransaction();
    }

    assertFalse(userRepo.findById(userId1).isPresent());
  }

  @Test
  void shouldAutoRollbackOnClose() throws Exception {
    UserRepository userRepo = context.getUserRepository();

    try (UnitOfWork uow = new UnitOfWork(dbManager.getConnectionPool())) {
      uow.begin();

      User user =
          User.builder()
              .username("testuser")
              .email("test@example.com")
              .createdAt(LocalDateTime.now())
              .build();

      uow.registerNew(user);
    }

    UnitOfWork newUow = new UnitOfWork(dbManager.getConnectionPool());
    newUow.begin();
    assertTrue(newUow.getNewEntities().isEmpty());
  }

  @Test
  void shouldProvideImmutableEntitySets() {
    UnitOfWork uow = context.beginTransaction();

    User user = User.builder().username("user").email("user@example.com").build();
    uow.registerNew(user);

    assertThrows(UnsupportedOperationException.class, () -> uow.getNewEntities().clear());
    assertThrows(UnsupportedOperationException.class, () -> uow.getDirtyEntities().clear());
    assertThrows(UnsupportedOperationException.class, () -> uow.getRemovedEntities().clear());
  }
}
