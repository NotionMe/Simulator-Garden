package ua.notion.presentation.controller;

import java.io.IOException;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType;
import ua.notion.presentation.game.assets.PlantIconCache;
import ua.notion.presentation.viewmodel.SeedInventoryViewModel;
import ua.notion.presentation.viewmodel.SeedInventoryViewModel.SeedItem;

public class SeedPlantingModalController {

  @FXML private Pane overlayRoot;
  @FXML private HBox pickerBar;
  @FXML private VBox emptyState;

  private SeedInventoryViewModel viewModel;
  private SeedSelectionCallback callback;
  private Runnable onCloseCallback;

  public void initialize(
      SeedInventoryViewModel viewModel, SeedSelectionCallback callback, Runnable onCloseCallback) {
    this.viewModel = viewModel;
    this.callback = callback;
    this.onCloseCallback = onCloseCallback;
    populatePicker();
  }

  private void populatePicker() {
    var availableSeeds = viewModel.getAvailableSeeds();

    if (availableSeeds.isEmpty()) {
      pickerBar.setVisible(false);
      pickerBar.setManaged(false);
      emptyState.setVisible(true);
      emptyState.setManaged(true);
      return;
    }

    pickerBar.getChildren().clear();
    for (SeedItem seedItem : availableSeeds) {
      pickerBar.getChildren().add(createSeedSlot(seedItem));
    }
    pickerBar.setVisible(true);
    pickerBar.setManaged(true);
    emptyState.setVisible(false);
    emptyState.setManaged(false);
  }

  private StackPane createSeedSlot(SeedItem seedItem) {
    StackPane slot = new StackPane();
    slot.getStyleClass().add("seed-slot");
    slot.setAlignment(Pos.CENTER);

    ImageView icon = new ImageView(PlantIconCache.getIcon(seedItem.getPlantType()));
    icon.getStyleClass().add("seed-slot-icon");

    Label countLabel = new Label("×" + seedItem.getCount());
    countLabel.getStyleClass().add("seed-slot-count");
    StackPane.setAlignment(countLabel, Pos.TOP_RIGHT);

    seedItem
        .countProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              countLabel.setText("×" + newVal);
              slot.setDisable(newVal.intValue() <= 0);
            });

    Tooltip tooltip = new Tooltip(seedItem.getDisplayName() + " · " + seedItem.getGrowthInfo());
    Tooltip.install(slot, tooltip);

    slot.getChildren().addAll(icon, countLabel);

    slot.setOnMouseClicked(
        event -> {
          event.consume();
          if (callback != null) {
            callback.onSeedSelected(seedItem.getPlantType());
          }
          close();
        });

    return slot;
  }

  public void close() {
    if (overlayRoot != null && overlayRoot.getParent() instanceof Pane parent) {
      parent.getChildren().remove(overlayRoot);
    } else if (overlayRoot != null && overlayRoot.getParent() instanceof StackPane stack) {
      stack.getChildren().remove(overlayRoot);
    }
    if (onCloseCallback != null) {
      onCloseCallback.run();
    }
  }

  public Pane getOverlayRoot() {
    return overlayRoot;
  }

  public HBox getPickerBar() {
    return pickerBar;
  }

  public VBox getEmptyState() {
    return emptyState;
  }

  public static Pane createPicker(
      SeedInventoryViewModel viewModel,
      SeedSelectionCallback callback,
      Runnable onCloseCallback,
      Consumer<SeedPlantingModalController> onReady) {
    try {
      FXMLLoader loader =
          new FXMLLoader(
              SeedPlantingModalController.class.getResource("/fxml/seed_planting_modal.fxml"));
      Pane overlay = loader.load();

      overlay
          .getStylesheets()
          .add(
              SeedPlantingModalController.class
                  .getResource("/css/seed_modal.css")
                  .toExternalForm());

      SeedPlantingModalController controller = loader.getController();
      controller.initialize(viewModel, callback, onCloseCallback);
      if (onReady != null) {
        onReady.accept(controller);
      }
      return overlay;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @FunctionalInterface
  public interface SeedSelectionCallback {
    void onSeedSelected(PlantType plantType);
  }
}
