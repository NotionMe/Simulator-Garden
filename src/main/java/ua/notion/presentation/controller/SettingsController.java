package ua.notion.presentation.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import ua.notion.presentation.ui.AppSettings;

public class SettingsController {

  @FXML private CheckBox soundEnabledCheck;
  @FXML private Slider volumeSlider;
  @FXML private CheckBox debugHintsCheck;
  @FXML private ComboBox<String> languageCombo;

  private Runnable onClose;

  public void setOnClose(Runnable onClose) {
    this.onClose = onClose;
  }

  @FXML
  private void initialize() {
    AppSettings settings = AppSettings.getInstance();

    soundEnabledCheck.selectedProperty().bindBidirectional(settings.soundEnabledProperty());
    volumeSlider.valueProperty().bindBidirectional(settings.masterVolumeProperty());
    debugHintsCheck.selectedProperty().bindBidirectional(settings.showDebugHintsProperty());

    languageCombo.setItems(FXCollections.observableArrayList("uk", "en"));
    languageCombo.valueProperty().bindBidirectional(settings.languageProperty());
    if (languageCombo.getValue() == null) {
      languageCombo.setValue("uk");
    }

    volumeSlider.disableProperty().bind(soundEnabledCheck.selectedProperty().not());
  }

  @FXML
  private void handleSave() {
    close();
  }

  @FXML
  private void handleClose() {
    close();
  }

  @FXML
  private void handleBackdropClick() {
    close();
  }

  private void close() {
    if (onClose != null) {
      onClose.run();
    }
  }
}
