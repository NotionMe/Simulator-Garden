package ua.notion.presentation.controller;

import java.io.IOException;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ua.notion.presentation.game.assets.PlantIconCache;
import ua.notion.presentation.viewmodel.PlayerInventoryViewModel;
import ua.notion.presentation.viewmodel.ServerBackedInventory.InventoryEntry;

public class PlayerInventoryController {

  @FXML private AnchorPane overlayRoot;
  @FXML private VBox inventoryPanel;
  @FXML private ScrollPane itemsScroll;
  @FXML private FlowPane itemsFlow;
  @FXML private VBox emptyState;
  @FXML private Label totalLabel;

  private PlayerInventoryViewModel viewModel;
  private Runnable onCloseCallback;

  public void initialize(PlayerInventoryViewModel viewModel, Runnable onCloseCallback) {
    this.viewModel = viewModel;
    this.onCloseCallback = onCloseCallback;
    refresh();
  }

  public void refresh() {
    itemsFlow.getChildren().clear();
    var items = viewModel.getAllItems();

    if (items.isEmpty()) {
      if (itemsScroll != null) {
        itemsScroll.setVisible(false);
        itemsScroll.setManaged(false);
      }
      emptyState.setVisible(true);
      emptyState.setManaged(true);
      totalLabel.setText("0");
      return;
    }

    emptyState.setVisible(false);
    emptyState.setManaged(false);
    if (itemsScroll != null) {
      itemsScroll.setVisible(true);
      itemsScroll.setManaged(true);
    }

    int total = 0;
    for (InventoryEntry entry : items) {
      total += entry.getCount();
      itemsFlow.getChildren().add(createSlot(entry));
    }
    totalLabel.setText(String.valueOf(total));
  }

  private StackPane createSlot(InventoryEntry entry) {
    StackPane slot = new StackPane();
    slot.getStyleClass().add("inventory-slot");
    slot.setAlignment(Pos.CENTER);

    ImageView icon = new ImageView(PlantIconCache.getHarvestIcon(entry.getPlantType()));
    icon.getStyleClass().add("inventory-slot-icon");

    Label count = new Label(String.valueOf(entry.getCount()));
    count.getStyleClass().add("inventory-slot-count");
    StackPane.setAlignment(count, Pos.TOP_RIGHT);

    entry.countProperty().addListener((obs, o, n) -> count.setText(String.valueOf(n.intValue())));

    Tooltip.install(slot, new Tooltip(entry.getDisplayName() + " ×" + entry.getCount()));

    slot.getChildren().addAll(icon, count);
    return slot;
  }

  @FXML
  public void close() {
    if (overlayRoot == null) {
      return;
    }
    if (overlayRoot.getParent() instanceof Pane parent) {
      parent.getChildren().remove(overlayRoot);
    } else if (overlayRoot.getParent() instanceof javafx.scene.layout.StackPane stack) {
      stack.getChildren().remove(overlayRoot);
    }
    if (onCloseCallback != null) {
      onCloseCallback.run();
      onCloseCallback = null;
    }
  }

  public Pane getOverlayRoot() {
    return overlayRoot;
  }

  public VBox getInventoryPanel() {
    return inventoryPanel;
  }

  public static Pane createPanel(
      PlayerInventoryViewModel viewModel,
      Runnable onCloseCallback,
      Consumer<PlayerInventoryController> onReady) {
    try {
      FXMLLoader loader =
          new FXMLLoader(
              PlayerInventoryController.class.getResource("/fxml/player_inventory.fxml"));
      AnchorPane overlay = loader.load();
      overlay
          .getStylesheets()
          .add(
              PlayerInventoryController.class
                  .getResource("/css/player_inventory.css")
                  .toExternalForm());

      PlayerInventoryController controller = loader.getController();
      controller.initialize(viewModel, onCloseCallback);
      if (onReady != null) {
        onReady.accept(controller);
      }
      return overlay;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
