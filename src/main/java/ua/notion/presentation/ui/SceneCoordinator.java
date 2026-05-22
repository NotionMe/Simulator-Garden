package ua.notion.presentation.ui;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import ua.notion.domain.entity.User;
import ua.notion.presentation.controller.MenuController;
import ua.notion.presentation.controller.SettingsController;

/** Single-window navigation: swap content in place, show overlays on top (no new Stage/Dialog). */
public final class SceneCoordinator {

  private static final String APP_CSS = "/css/app.css";

  private final Stage stage;
  private final StackPane shell;
  private final StackPane contentHost;
  private final Pane overlayHost;

  private Runnable onContentDispose;
  private Parent boundContent;

  public SceneCoordinator(Stage stage) {
    this.stage = stage;
    this.contentHost = new StackPane();
    this.overlayHost = new Pane();
    overlayHost.setPickOnBounds(false);
    this.shell = new StackPane(contentHost, overlayHost);
    StackPane.setAlignment(contentHost, Pos.CENTER);

    bindHostToStage(contentHost);

    Scene scene = new Scene(shell);
    URL css = getClass().getResource(APP_CSS);
    if (css != null) {
      scene.getStylesheets().add(css.toExternalForm());
    }
    stage.setScene(scene);
    stage.setMinWidth(800);
    stage.setMinHeight(600);
  }

  public static SceneCoordinator of(Stage stage) {
    Object data = stage.getUserData();
    if (data instanceof SceneCoordinator coordinator) {
      return coordinator;
    }
    throw new IllegalStateException("Stage has no SceneCoordinator attached");
  }

  /** Resolve coordinator from a node on the scene graph, or from the app's primary stage. */
  public static SceneCoordinator forNode(Node node) {
    if (node != null) {
      Scene scene = node.getScene();
      if (scene != null) {
        return of((Stage) scene.getWindow());
      }
    }
    return anyAttached();
  }

  public static SceneCoordinator anyAttached() {
    for (Window window : Window.getWindows()) {
      if (window instanceof Stage stage) {
        Object data = stage.getUserData();
        if (data instanceof SceneCoordinator coordinator) {
          return coordinator;
        }
      }
    }
    throw new IllegalStateException("No SceneCoordinator attached to any stage");
  }

  public static void attach(Stage stage, SceneCoordinator coordinator) {
    stage.setUserData(coordinator);
  }

  public Stage getStage() {
    return stage;
  }

  public void setOnContentDispose(Runnable onContentDispose) {
    this.onContentDispose = onContentDispose;
  }

  public void setContent(Parent root) {
    disposeCurrentContent();
    boundContent = root;
    contentHost.getChildren().setAll(root);
    StackPane.setAlignment(root, Pos.TOP_LEFT);
    fillContentToHost(root);
  }

  public FXMLLoader loadContent(String fxmlPath) throws IOException {
    hideOverlay();
    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
    Parent root = loader.load();
    setContent(root);
    return loader;
  }

  public void addStylesheet(String cssPath) {
    URL url = getClass().getResource(cssPath);
    if (url != null && !stage.getScene().getStylesheets().contains(url.toExternalForm())) {
      stage.getScene().getStylesheets().add(url.toExternalForm());
    }
  }

  public void showOverlay(Node overlay) {
    overlayHost.getChildren().setAll(overlay);
    overlayHost.setVisible(true);
    overlayHost.setManaged(true);
  }

  public void hideOverlay() {
    overlayHost.getChildren().clear();
  }

  public void navigateToMenu(User user) throws IOException {
    hideOverlay();
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
    Parent root = loader.load();
    MenuController menuController = loader.getController();
    menuController.setCurrentUser(user);
    setContent(root);
    addStylesheet("/css/menu.css");
    stage.setTitle("Garden Simulator - Main Menu");
  }

  public void showSettingsOverlay() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings_overlay.fxml"));
      Parent overlay = loader.load();
      SettingsController controller = loader.getController();
      controller.setOnClose(this::hideOverlay);
      URL settingsCss = getClass().getResource("/css/settings.css");
      if (settingsCss != null
          && !stage.getScene().getStylesheets().contains(settingsCss.toExternalForm())) {
        stage.getScene().getStylesheets().add(settingsCss.toExternalForm());
      }
      showOverlay(overlay);
    } catch (IOException e) {
      showMessageOverlay("Settings", "Failed to open settings: " + e.getMessage(), false);
    }
  }

  public void showRegisterOverlay(Parent registerRoot, Runnable onClose) {
    StackPane wrapper = new StackPane();
    wrapper.getStyleClass().add("overlay-wrapper");
    Region backdrop = new Region();
    backdrop.getStyleClass().add("overlay-backdrop");
    backdrop.setOnMouseClicked(
        e -> {
          hideOverlay();
          if (onClose != null) {
            onClose.run();
          }
        });
    StackPane panel = new StackPane(registerRoot);
    panel.getStyleClass().add("overlay-panel-auth");
    panel.setOnMouseClicked(e -> e.consume());
    wrapper.getChildren().addAll(backdrop, panel);
    showOverlay(wrapper);
  }

  public void showMessageOverlay(String title, String message, boolean success) {
    StackPane wrapper = new StackPane();
    wrapper.getStyleClass().add("overlay-wrapper");
    Region backdrop = new Region();
    backdrop.getStyleClass().add("overlay-backdrop");
    backdrop.setOnMouseClicked(e -> hideOverlay());

    javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(12);
    card.getStyleClass().add(success ? "overlay-card-success" : "overlay-card-info");
    card.setAlignment(Pos.CENTER);
    card.setMaxWidth(360);

    javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
    titleLabel.getStyleClass().add("overlay-card-title");
    javafx.scene.control.Label msgLabel = new javafx.scene.control.Label(message);
    msgLabel.getStyleClass().add("overlay-card-message");
    msgLabel.setWrapText(true);
    javafx.scene.control.Button ok = new javafx.scene.control.Button("OK");
    ok.getStyleClass().add("overlay-primary-button");
    ok.setOnAction(e -> hideOverlay());
    card.getChildren().addAll(titleLabel, msgLabel, ok);

    StackPane panel = new StackPane(card);
    panel.getStyleClass().add("overlay-panel-center");
    panel.setOnMouseClicked(e -> e.consume());
    wrapper.getChildren().addAll(backdrop, panel);
    showOverlay(wrapper);
  }

  private void bindHostToStage(Region host) {
    host.prefWidthProperty().bind(stage.widthProperty());
    host.prefHeightProperty().bind(stage.heightProperty());
    host.minWidthProperty().bind(stage.widthProperty());
    host.minHeightProperty().bind(stage.heightProperty());
    host.maxWidthProperty().bind(stage.widthProperty());
    host.maxHeightProperty().bind(stage.heightProperty());
  }

  private void fillContentToHost(Parent root) {
    if (!(root instanceof Region region)) {
      return;
    }
    region.prefWidthProperty().bind(contentHost.widthProperty());
    region.prefHeightProperty().bind(contentHost.heightProperty());
    region.minWidthProperty().bind(contentHost.widthProperty());
    region.minHeightProperty().bind(contentHost.heightProperty());
    region.maxWidthProperty().bind(contentHost.widthProperty());
    region.maxHeightProperty().bind(contentHost.heightProperty());
  }

  private void disposeCurrentContent() {
    if (onContentDispose != null) {
      onContentDispose.run();
      onContentDispose = null;
    }
    unbindContent(boundContent);
    boundContent = null;
  }

  private void unbindContent(Parent root) {
    if (root instanceof Region region) {
      unbindIfBound(region.prefWidthProperty());
      unbindIfBound(region.prefHeightProperty());
      unbindIfBound(region.minWidthProperty());
      unbindIfBound(region.maxWidthProperty());
      unbindIfBound(region.minHeightProperty());
      unbindIfBound(region.maxHeightProperty());
    }
  }

  private static void unbindIfBound(javafx.beans.property.DoubleProperty property) {
    if (property.isBound()) {
      property.unbind();
    }
  }
}
