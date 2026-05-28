package ua.notion.presentation.game.weather;

import java.util.Random;

public class WeatherManager {
  public enum WeatherType {
    SUNNY,
    RAINY
  }

  private WeatherType currentWeather = WeatherType.SUNNY;
  private double timeUntilNextChange;
  private final Random random = new Random();

  private static final double MIN_WEATHER_DURATION = 15.0;
  private static final double MAX_WEATHER_DURATION = 45.0;

  public WeatherManager() {
    scheduleNextChange();
  }

  public void update(double deltaSeconds) {
    timeUntilNextChange -= deltaSeconds;
    if (timeUntilNextChange <= 0) {
      toggleWeather();
    }
  }

  public void toggleWeatherDebug() {
    if (currentWeather == WeatherType.SUNNY) {
      currentWeather = WeatherType.RAINY;
    } else {
      currentWeather = WeatherType.SUNNY;
    }
    scheduleNextChange();
  }

  private void toggleWeather() {
    if (currentWeather == WeatherType.SUNNY) {
      if (random.nextDouble() < 0.3) { // 30% chance for rain
        currentWeather = WeatherType.RAINY;
      }
    } else {
      currentWeather = WeatherType.SUNNY;
    }
    scheduleNextChange();
  }

  private void scheduleNextChange() {
    timeUntilNextChange =
        MIN_WEATHER_DURATION + (MAX_WEATHER_DURATION - MIN_WEATHER_DURATION) * random.nextDouble();
  }

  public WeatherType getCurrentWeather() {
    return currentWeather;
  }

  public boolean isRaining() {
    return currentWeather == WeatherType.RAINY;
  }

  public double getGrowthMultiplier() {
    return isRaining() ? 2.5 : 1.0;
  }
}
