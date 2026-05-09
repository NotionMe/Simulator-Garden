package ua.notion.presentation;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ua.notion.domain.entity.User;
import ua.notion.presentation.controller.MenuController;

public class MenuTestApp extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
    Parent root = loader.load();

    MenuController controller = loader.getController();
    User testUser = User.builder().id(1).username("TestPlayer").email("test@example.com").build();
    controller.setCurrentUser(testUser);

    Scene scene = new Scene(root, 800, 600);

    primaryStage.setTitle("Garden Simulator - Main Menu");
    primaryStage.setScene(scene);
    primaryStage.setMinWidth(800);
    primaryStage.setMinHeight(600);
    primaryStage.setMaxWidth(1920);
    primaryStage.setMaxHeight(1080);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
