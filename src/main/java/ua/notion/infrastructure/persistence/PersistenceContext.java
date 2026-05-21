package ua.notion.infrastructure.persistence;

import ua.notion.infrastructure.persistence.contract.AchievementRepository;
import ua.notion.infrastructure.persistence.contract.GardenRepository;
import ua.notion.infrastructure.persistence.contract.PlantInstanceRepository;
import ua.notion.infrastructure.persistence.contract.PlantRepository;
import ua.notion.infrastructure.persistence.contract.TaskRepository;
import ua.notion.infrastructure.persistence.contract.UserRepository;
import ua.notion.infrastructure.persistence.contract.WeatherEventRepository;
import ua.notion.infrastructure.persistence.impl.AchievementRepositoryImpl;
import ua.notion.infrastructure.persistence.impl.GardenRepositoryImpl;
import ua.notion.infrastructure.persistence.impl.PlantInstanceRepositoryImpl;
import ua.notion.infrastructure.persistence.impl.PlantRepositoryImpl;
import ua.notion.infrastructure.persistence.impl.TaskRepositoryImpl;
import ua.notion.infrastructure.persistence.impl.UserRepositoryImpl;
import ua.notion.infrastructure.persistence.impl.WeatherEventRepositoryImpl;
import ua.notion.infrastructure.websocket.WebSocketApiClient;

public class PersistenceContext {
  private final WebSocketApiClient apiClient;

  private final UserRepository userRepository;
  private final GardenRepository gardenRepository;
  private final PlantRepository plantRepository;
  private final PlantInstanceRepository plantInstanceRepository;
  private final TaskRepository taskRepository;
  private final WeatherEventRepository weatherEventRepository;
  private final AchievementRepository achievementRepository;

  public PersistenceContext(WebSocketApiClient apiClient) {
    this.apiClient = apiClient;
    this.userRepository = new UserRepositoryImpl(apiClient);
    this.gardenRepository = new GardenRepositoryImpl(apiClient);
    this.plantRepository = new PlantRepositoryImpl(apiClient);
    this.plantInstanceRepository = new PlantInstanceRepositoryImpl(apiClient);
    this.taskRepository = new TaskRepositoryImpl(apiClient);
    this.weatherEventRepository = new WeatherEventRepositoryImpl(apiClient);
    this.achievementRepository = new AchievementRepositoryImpl(apiClient);
  }

  public UserRepository getUserRepository() {
    return userRepository;
  }

  public GardenRepository getGardenRepository() {
    return gardenRepository;
  }

  public PlantRepository getPlantRepository() {
    return plantRepository;
  }

  public PlantInstanceRepository getPlantInstanceRepository() {
    return plantInstanceRepository;
  }

  public TaskRepository getTaskRepository() {
    return taskRepository;
  }

  public WeatherEventRepository getWeatherEventRepository() {
    return weatherEventRepository;
  }

  public AchievementRepository getAchievementRepository() {
    return achievementRepository;
  }

  public void close() {
    try {
      apiClient.close();
    } catch (Exception e) {
      // Ignore
    }
  }
}
