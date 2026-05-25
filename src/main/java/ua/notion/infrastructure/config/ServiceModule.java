package ua.notion.infrastructure.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import ua.notion.domain.service.AchievementService;
import ua.notion.domain.service.GardenService;
import ua.notion.domain.service.PlantInstanceService;
import ua.notion.domain.service.PlantService;
import ua.notion.domain.service.PlayerInventoryItemService;
import ua.notion.domain.service.TaskService;
import ua.notion.domain.service.UserService;
import ua.notion.domain.service.WeatherEventService;
import ua.notion.domain.service.auth.AuthenticationService;
import ua.notion.domain.service.auth.WebSocketAuthenticationService;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.websocket.WebSocketApiClient;
import ua.notion.presentation.game.catalog.PlantTypeResolver;

public class ServiceModule extends AbstractModule {

  @Override
  protected void configure() {
    // Bindings are handled via @Provides methods
  }

  @Provides
  @Singleton
  UserService provideUserService(PersistenceContext context) {
    return new UserService(context);
  }

  @Provides
  @Singleton
  GardenService provideGardenService(PersistenceContext context) {
    return new GardenService(context);
  }

  @Provides
  @Singleton
  PlantService providePlantService(PersistenceContext context) {
    return new PlantService(context);
  }

  @Provides
  @Singleton
  PlantInstanceService providePlantInstanceService(PersistenceContext context) {
    return new PlantInstanceService(context);
  }

  @Provides
  @Singleton
  TaskService provideTaskService(PersistenceContext context) {
    return new TaskService(context);
  }

  @Provides
  @Singleton
  WeatherEventService provideWeatherEventService(PersistenceContext context) {
    return new WeatherEventService(context);
  }

  @Provides
  @Singleton
  AchievementService provideAchievementService(PersistenceContext context) {
    return new AchievementService(context);
  }

  @Provides
  @Singleton
  AuthenticationService provideAuthenticationService(WebSocketApiClient apiClient) {
    return new WebSocketAuthenticationService(apiClient);
  }

  @Provides
  @Singleton
  PlayerInventoryItemService providePlayerInventoryItemService(PersistenceContext context) {
    return new PlayerInventoryItemService(context);
  }

  @Provides
  @Singleton
  PlantTypeResolver providePlantTypeResolver(PlantService plantService) {
    return new PlantTypeResolver(plantService);
  }
}
