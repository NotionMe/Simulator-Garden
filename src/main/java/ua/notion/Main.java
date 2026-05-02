package ua.notion;

import java.time.LocalDateTime;
import ua.notion.domain.entity.User;
import ua.notion.infrastructure.persistence.contract.UserRepository;
import ua.notion.infrastructure.persistence.impl.UserRepositoryImpl;
import ua.notion.infrastructure.persistence.util.ConnectionPool;

public class Main {
  public static void main(String[] args) {
    ConnectionPool.PoolConfig config =
        new ConnectionPool.PoolConfig.Builder().withUrl("jdbc:sqlite:./data/garden.db").build();

    ConnectionPool connectionPool = new ConnectionPool(config);

    try {
      UserRepository userRepository = new UserRepositoryImpl(connectionPool);

      User newUser =
          User.builder()
              .username("testuser")
              .email("test@example.com")
              .createdAt(LocalDateTime.now())
              .build();

      userRepository.save(newUser);
      System.out.println("User saved: " + newUser);

      var users = userRepository.findAll();
      System.out.println("Total users: " + users.size());

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      connectionPool.shutdown();
    }
  }
}
