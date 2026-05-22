package ua.notion.presentation.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** In-memory app settings (can be persisted to Preferences/DB later). */
public final class AppSettings {

  private static final AppSettings INSTANCE = new AppSettings();

  private final BooleanProperty soundEnabled = new SimpleBooleanProperty(true);
  private final DoubleProperty masterVolume = new SimpleDoubleProperty(0.8);
  private final BooleanProperty showDebugHints = new SimpleBooleanProperty(true);
  private final StringProperty language = new SimpleStringProperty("uk");

  private AppSettings() {}

  public static AppSettings getInstance() {
    return INSTANCE;
  }

  public BooleanProperty soundEnabledProperty() {
    return soundEnabled;
  }

  public DoubleProperty masterVolumeProperty() {
    return masterVolume;
  }

  public BooleanProperty showDebugHintsProperty() {
    return showDebugHints;
  }

  public StringProperty languageProperty() {
    return language;
  }
}
