package ua.notion.infrastructure.config;

import com.google.inject.Injector;

/** Application-wide Guice injector so auth token and WebSocket client stay shared. */
public final class AppInjector {

  private static volatile Injector injector;

  private AppInjector() {}

  public static void init(Injector guiceInjector) {
    injector = guiceInjector;
  }

  public static Injector get() {
    Injector current = injector;
    if (current == null) {
      throw new IllegalStateException(
          "Application injector not initialized. Start via GardenSimulatorApp.");
    }
    return current;
  }
}
