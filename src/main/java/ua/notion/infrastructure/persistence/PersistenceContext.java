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
import ua.notion.infrastructure.persistence.util.ConnectionPool;

public class PersistenceContext {
  private final ConnectionPool connectionPool;
  private UnitOfWork unitOfWork;

  private final UserRepository userRepository;
  private final GardenRepository gardenRepository;
  private final PlantRepository plantRepository;
  private final PlantInstanceRepository plantInstanceRepository;
  private final TaskRepository taskRepository;
  private final WeatherEventRepository weatherEventRepository;
  private final AchievementRepository achievementRepository;

  public PersistenceContext(ConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
    this.userRepository = new UserRepositoryImpl(connectionPool);
    this.gardenRepository = new GardenRepositoryImpl(connectionPool);
    this.plantRepository = new PlantRepositoryImpl(connectionPool);
    this.plantInstanceRepository = new PlantInstanceRepositoryImpl(connectionPool);
    this.taskRepository = new TaskRepositoryImpl(connectionPool);
    this.weatherEventRepository = new WeatherEventRepositoryImpl(connectionPool);
    this.achievementRepository = new AchievementRepositoryImpl(connectionPool);
  }

  public UnitOfWork beginTransaction() {
    if (unitOfWork != null && unitOfWork.isActive()) {
      throw new IllegalStateException("Transaction already active");
    }
    unitOfWork = new UnitOfWork(connectionPool);
    unitOfWork.begin();

    // Set UnitOfWork for all repositories
    ((GenericRepository<?, ?>) userRepository).setUnitOfWork(unitOfWork);
    ((GenericRepository<?, ?>) gardenRepository).setUnitOfWork(unitOfWork);
    ((GenericRepository<?, ?>) plantRepository).setUnitOfWork(unitOfWork);
    ((GenericRepository<?, ?>) plantInstanceRepository).setUnitOfWork(unitOfWork);
    ((GenericRepository<?, ?>) taskRepository).setUnitOfWork(unitOfWork);
    ((GenericRepository<?, ?>) weatherEventRepository).setUnitOfWork(unitOfWork);
    ((GenericRepository<?, ?>) achievementRepository).setUnitOfWork(unitOfWork);

    return unitOfWork;
  }

  public void commitTransaction() {
    if (unitOfWork == null || !unitOfWork.isActive()) {
      throw new IllegalStateException("No active transaction");
    }
    unitOfWork.commit();
    clearUnitOfWorkFromRepositories();
  }

  public void rollbackTransaction() {
    if (unitOfWork != null && unitOfWork.isActive()) {
      unitOfWork.rollback();
    }
    clearUnitOfWorkFromRepositories();
  }

  private void clearUnitOfWorkFromRepositories() {
    ((GenericRepository<?, ?>) userRepository).setUnitOfWork(null);
    ((GenericRepository<?, ?>) gardenRepository).setUnitOfWork(null);
    ((GenericRepository<?, ?>) plantRepository).setUnitOfWork(null);
    ((GenericRepository<?, ?>) plantInstanceRepository).setUnitOfWork(null);
    ((GenericRepository<?, ?>) taskRepository).setUnitOfWork(null);
    ((GenericRepository<?, ?>) weatherEventRepository).setUnitOfWork(null);
    ((GenericRepository<?, ?>) achievementRepository).setUnitOfWork(null);
  }

  public UnitOfWork getUnitOfWork() {
    return unitOfWork;
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

  public ConnectionPool getConnectionPool() {
    return connectionPool;
  }

  public void close() {
    if (unitOfWork != null && unitOfWork.isActive()) {
      unitOfWork.rollback();
    }
    connectionPool.shutdown();
  }
}
