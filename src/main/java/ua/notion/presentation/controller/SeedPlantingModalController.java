package ua.notion.presentation.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ua.notion.presentation.viewmodel.SeedInventoryViewModel;
import ua.notion.presentation.viewmodel.SeedInventoryViewModel.SeedItem;

public class SeedPlantingModalController {

  @FXML private GridPane seedGrid;
  @FXML private VBox emptyState;

  private SeedInventoryViewModel viewModel;
  private StackPane modalRoot;
  private SeedSelectionCallback callback;
  private Runnable onCloseCallback;

  public void initialize(
      SeedInventoryViewModel viewModel,
      StackPane modalRoot,
      SeedSelectionCallback callback,
      Runnable onCloseCallback) {
    this.viewModel = viewModel;
    this.modalRoot = modalRoot;
    this.callback = callback;
    this.onCloseCallback = onCloseCallback;

    populateSeedGrid();
  }

  private void populateSeedGrid() {
    var availableSeeds = viewModel.getAvailableSeeds();

    if (availableSeeds.isEmpty()) {
      seedGrid.setVisible(false);
      seedGrid.setManaged(false);
      emptyState.setVisible(true);
      emptyState.setManaged(true);
      return;
    }

    int column = 0;
    int row = 0;
    int maxColumns = 3;

    for (SeedItem seedItem : availableSeeds) {
      VBox seedCard = createSeedCard(seedItem);
      seedGrid.add(seedCard, column, row);

      column++;
      if (column >= maxColumns) {
        column = 0;
        row++;
      }
    }
  }

  private VBox createSeedCard(SeedItem seedItem) {
    VBox card = new VBox(12);
    card.setAlignment(Pos.CENTER);
    card.getStyleClass().add("seed-card");
    card.setMinWidth(120);
    card.setMaxWidth(120);

    // Seed icon placeholder
    VBox iconBox = new VBox();
    iconBox.getStyleClass().add("seed-icon");
    iconBox.setAlignment(Pos.CENTER);

    // Seed name
    Label nameLabel = new Label(seedItem.getDisplayName());
    nameLabel.getStyleClass().add("seed-name");

    // Seed count badge
    VBox countBadge = new VBox();
    countBadge.getStyleClass().add("seed-count");
    countBadge.setAlignment(Pos.CENTER);

    Label countLabel = new Label("×" + seedItem.getCount());
    countLabel.getStyleClass().add("seed-count-text");
    countBadge.getChildren().add(countLabel);

    // Bind count to property for live updates
    seedItem
        .countProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              countLabel.setText("×" + newVal);
              if (newVal.intValue() <= 0) {
                card.setDisable(true);
              }
            });

    // Growth info
    Label infoLabel = new Label(seedItem.getGrowthInfo());
    infoLabel.getStyleClass().add("seed-info");

    card.getChildren().addAll(iconBox, nameLabel, countBadge, infoLabel);

    // Click handler
    card.setOnMouseClicked(
        event -> {
          if (callback != null) {
            callback.onSeedSelected(seedItem.getPlantType());
          }
          closeModal();
        });

    return card;
  }

  @FXML
  private void closeModal() {
    if (modalRoot != null && modalRoot.getParent() instanceof StackPane) {
      ((StackPane) modalRoot.getParent()).getChildren().remove(modalRoot);
    }
    if (onCloseCallback != null) {
      onCloseCallback.run();
    }
  }

  public static StackPane createModal(
      SeedInventoryViewModel viewModel, SeedSelectionCallback callback, Runnable onCloseCallback) {
    try {
      FXMLLoader loader =
          new FXMLLoader(
              SeedPlantingModalController.class.getResource("/fxml/seed_planting_modal.fxml"));
      StackPane modalRoot = loader.load();

      SeedPlantingModalController controller = loader.getController();
      controller.initialize(viewModel, modalRoot, callback, onCloseCallback);

      // Load CSS
      modalRoot
          .getStylesheets()
          .add(
              SeedPlantingModalController.class
                  .getResource("/css/seed_modal.css")
                  .toExternalForm());

      return modalRoot;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @FunctionalInterface
  public interface SeedSelectionCallback {
    void onSeedSelected(ua.notion.presentation.game.assets.PlantBasesAtlas.PlantType plantType);
  }
}
