package ua.notion.infrastructure.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.io.File;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.infrastructure.persistence.contract.AchievementRepository;
import ua.notion.infrastructure.persistence.contract.GardenRepository;
import ua.notion.infrastructure.persistence.contract.PlantInstanceRepository;
import ua.notion.infrastructure.persistence.contract.PlantRepository;
import ua.notion.infrastructure.persistence.contract.TaskRepository;
import ua.notion.infrastructure.persistence.contract.UserRepository;
import ua.notion.infrastructure.persistence.contract.WeatherEventRepository;
import ua.notion.infrastructure.persistence.util.ConnectionPool;
import ua.notion.infrastructure.persistence.util.DatabaseInitializer;

public class PersistenceModule extends AbstractModule {

  @Override
  protected void configure() {
    // Bindings are handled via @Provides methods
  }

  @Provides
  @Singleton
  ConnectionPool provideConnectionPool() {
    String databaseUrl = System.getProperty("db.url", getDefaultDatabasePath());
    DatabaseInitializer.ensureDatabase(databaseUrl);

    ConnectionPool.PoolConfig config =
        new ConnectionPool.PoolConfig.Builder().withUrl(databaseUrl).build();

    return new ConnectionPool(config);
  }

  private String getDefaultDatabasePath() {
    String userHome = System.getProperty("user.home");
    String appDir = userHome + "/.garden-simulator/data";

    File dir = new File(appDir);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    return "jdbc:sqlite:" + appDir + "/garden.db";
  }

  @Provides
  @Singleton
  PersistenceContext providePersistenceContext(ConnectionPool connectionPool) {
    return new PersistenceContext(connectionPool);
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
