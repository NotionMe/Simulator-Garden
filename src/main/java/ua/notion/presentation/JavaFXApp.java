package ua.notion.presentation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ua.notion.presentation.controller.GameController;

public class JavaFXApp extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game.fxml"));
    Parent root = loader.load();

    Scene scene = new Scene(root);
    primaryStage.setTitle("Garden Simulator");
    primaryStage.setScene(scene);
    primaryStage.setResizable(false);
    primaryStage.show();

    // Handle window close
    primaryStage.setOnCloseRequest(
        event -> {
          GameController controller = loader.getController();
          if (controller != null) {
            controller.shutdown();
          }
        });
  }

  public static void main(String[] args) {
    launch(args);
  }
}
