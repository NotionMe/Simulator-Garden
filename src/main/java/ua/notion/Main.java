package ua.notion;

import com.google.inject.Guice;
import com.google.inject.Injector;
import ua.notion.domain.entity.Garden;
import ua.notion.domain.entity.User;
import ua.notion.domain.service.GardenService;
import ua.notion.domain.service.UserService;
import ua.notion.infrastructure.config.PersistenceModule;
import ua.notion.infrastructure.config.ServiceModule;
import ua.notion.infrastructure.persistence.PersistenceContext;

public class Main {
  public static void main(String[] args) {
    // Create Guice injector with persistence and service modules
    Injector injector = Guice.createInjector(new PersistenceModule(), new ServiceModule());

    // Get services from injector
    UserService userService = injector.getInstance(UserService.class);
    GardenService gardenService = injector.getInstance(GardenService.class);
    PersistenceContext persistenceContext = injector.getInstance(PersistenceContext.class);

    try {
      // Create user using service
      User user =
          userService.createUser(
              "testuser_" + System.currentTimeMillis(),
              "test_" + System.currentTimeMillis() + "@example.com");
      System.out.println("User created: " + user);

      // Create garden for user
      Garden garden = gardenService.createGarden(user.getId(), "My First Garden", 10, 10);
      System.out.println("Garden created: " + garden);
      System.out.println("Total cells: " + gardenService.getTotalCells(garden));

      // Query all users
      var users = userService.getAllUsers();
      System.out.println("Total users: " + users.size());

      // Query gardens by user
      var gardens = gardenService.findGardensByUserId(user.getId());
      System.out.println("User's gardens: " + gardens.size());

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      persistenceContext.close();
    }
  }
}
