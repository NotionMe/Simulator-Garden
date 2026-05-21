package ua.notion.infrastructure.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.contract.AchievementRepository;
import ua.notion.infrastructure.persistence.contract.GardenRepository;
import ua.notion.infrastructure.persistence.contract.PlantInstanceRepository;
import ua.notion.infrastructure.persistence.contract.PlantRepository;
import ua.notion.infrastructure.persistence.contract.TaskRepository;
import ua.notion.infrastructure.persistence.contract.UserRepository;
import ua.notion.infrastructure.persistence.contract.WeatherEventRepository;
import ua.notion.infrastructure.websocket.WebSocketApiClient;

public class PersistenceModule extends AbstractModule {

  @Override
  protected void configure() {
    // Bindings are handled via @Provides methods
  }

  @Provides
  @Singleton
  WebSocketApiClient provideWebSocketApiClient() {
    WebSocketApiClient client = new WebSocketApiClient();
    try {
      client.connect();
    } catch (Exception e) {
      throw new RuntimeException("Failed to connect to WebSocket server", e);
    }
    return client;
  }

  @Provides
  @Singleton
  PersistenceContext providePersistenceContext(WebSocketApiClient apiClient) {
    return new PersistenceContext(apiClient);
  }

  @Provides
  UserRepository provideUserRepository(PersistenceContext context) {
    return context.getUserRepository();
  }

  @Provides
  GardenRepository provideGardenRepository(PersistenceContext context) {
    return context.getGardenRepository();
  }

  @Provides
  PlantRepository providePlantRepository(PersistenceContext context) {
    return context.getPlantRepository();
  }

  @Provides
  PlantInstanceRepository providePlantInstanceRepository(PersistenceContext context) {
    return context.getPlantInstanceRepository();
  }

  @Provides
  TaskRepository provideTaskRepository(PersistenceContext context) {
    return context.getTaskRepository();
  }

  @Provides
  WeatherEventRepository provideWeatherEventRepository(PersistenceContext context) {
    return context.getWeatherEventRepository();
  }

  @Provides
  AchievementRepository provideAchievementRepository(PersistenceContext context) {
    return context.getAchievementRepository();
  }
}
