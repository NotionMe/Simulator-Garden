package ua.notion.presentation.controller;

import java.io.IOException;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import ua.notion.domain.entity.User;
import ua.notion.presentation.game.assets.PlantIconCache;
import ua.notion.presentation.ui.SceneCoordinator;
import ua.notion.presentation.viewmodel.ShopViewModel;
import ua.notion.presentation.viewmodel.ShopViewModel.ShopItem;

public class ShopController {

  @FXML private Button backButton;
  @FXML private Label coinsLabel;
  @FXML private Label statusLabel;
  @FXML private ScrollPane buyScroll;
  @FXML private ScrollPane sellScroll;
  @FXML private FlowPane buyFlow;
  @FXML private FlowPane sellFlow;
  @FXML private VBox emptySellState;

  private final ShopViewModel viewModel;
  private User currentUser;

  public ShopController(ShopViewModel viewModel) {
    this.viewModel = viewModel;
  }

  @FXML
  private void initialize() {
    coinsLabel.textProperty().bind(viewModel.coinsProperty().asString("%d coins"));
    statusLabel.textProperty().bind(viewModel.statusMessageProperty());
    viewModel
        .getItems()
        .addListener((javafx.collections.ListChangeListener<ShopItem>) c -> refresh());
  }

  public void setCurrentUser(User user) {
    this.currentUser = user;
    if (user != null && user.getId() != null) {
      viewModel.setCurrentUserId(user.getId());
      refresh();
    }
  }

  private void refresh() {
    buyFlow.getChildren().clear();
    sellFlow.getChildren().clear();

    for (ShopItem item : viewModel.getItems()) {
      buyFlow.getChildren().add(createBuyCard(item));
      if (item.getOwnedCount() > 0) {
        sellFlow.getChildren().add(createSellCard(item));
      }
    }

    boolean hasSellItems = !sellFlow.getChildren().isEmpty();
    sellScroll.setVisible(hasSellItems);
    sellScroll.setManaged(hasSellItems);
    emptySellState.setVisible(!hasSellItems);
    emptySellState.setManaged(!hasSellItems);
  }

  private VBox createBuyCard(ShopItem item) {
    VBox card = createBaseCard(item);
    Label price = new Label(item.getBuyPrice() + " coins");
    price.getStyleClass().add("shop-price");

    Button buyButton = new Button("Buy");
    buyButton.getStyleClass().add("primary-button");
    buyButton
        .disableProperty()
        .bind(Bindings.lessThan(viewModel.coinsProperty(), item.getBuyPrice()));
    buyButton.setOnAction(
        event -> {
          viewModel.buy(item);
          refresh();
        });

    card.getChildren().addAll(price, buyButton);
    return card;
  }

  private VBox createSellCard(ShopItem item) {
    VBox card = createBaseCard(item);
    Label owned = new Label();
    owned.getStyleClass().add("shop-owned");
    owned.textProperty().bind(item.ownedCountProperty().asString("Owned: %d"));

    Label price = new Label("+" + item.getSellPrice() + " coins");
    price.getStyleClass().add("shop-price");

    Button sellButton = new Button("Sell");
    sellButton.getStyleClass().add("secondary-button");
    sellButton.disableProperty().bind(Bindings.lessThanOrEqual(item.ownedCountProperty(), 0));
    sellButton.setOnAction(
        event -> {
          viewModel.sell(item);
          refresh();
        });

    card.getChildren().addAll(owned, price, sellButton);
    return card;
  }

  private VBox createBaseCard(ShopItem item) {
    ImageView icon = new ImageView(PlantIconCache.getIcon(item.getPlantType()));
    icon.setFitWidth(48);
    icon.setFitHeight(48);
    icon.setPreserveRatio(true);

    Label name = new Label(item.getDisplayName());
    name.getStyleClass().add("shop-item-title");

    VBox card = new VBox(8, icon, name);
    card.getStyleClass().add("shop-card");
    card.setAlignment(Pos.CENTER);
    card.setPrefWidth(136);
    card.setMinHeight(164);
    return card;
  }

  @FXML
  private void handleBack() {
    try {
      SceneCoordinator.forNode(backButton).navigateToMenu(currentUser);
    } catch (IOException e) {
      viewModel.setStatusMessage("Failed to return to menu: " + e.getMessage());
    }
  }
}
