package ua.notion;

import ua.notion.presentation.GardenSimulatorApp;

/**
 * Клас-обгортка для запуску JavaFX-додатку через Fat JAR.
 *
 * <p>Проблема: При запуску JavaFX додатку з Fat JAR виникає помилка "Error: JavaFX runtime
 * components are missing" через те, що JavaFX перевіряє чи головний клас наслідує Application.
 *
 * <p>Рішення: Створити окремий клас-лаунчер, який не наслідує Application, а просто викликає
 * Application.launch() для справжнього JavaFX класу.
 */
public class Launcher {
  public static void main(String[] args) {
    GardenSimulatorApp.main(args);
  }
}
