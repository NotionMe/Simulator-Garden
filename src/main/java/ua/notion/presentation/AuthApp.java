package ua.notion.presentation;

import atlantafx.base.theme.PrimerLight;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ua.notion.domain.service.auth.AuthenticationService;
import ua.notion.infrastructure.config.PersistenceModule;
import ua.notion.infrastructure.config.ServiceModule;
import ua.notion.presentation.controller.auth.LoginController;

public class AuthApp extends Application {

  private Injector injector;

  @Override
  public void init() {
    injector = Guice.createInjector(new PersistenceModule(), new ServiceModule());
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));

    AuthenticationService authService = injector.getInstance(AuthenticationService.class);
    LoginController controller = new LoginController(authService);
    controller.setStage(primaryStage);
    controller.setOnLoginSuccess(
        () -> {
          System.out.println("Login successful! Transitioning to main app...");
          primaryStage.close();
        });

    loader.setController(controller);

    Parent root = loader.load();
    Scene scene = new Scene(root, 800, 700);
    scene.getStylesheets().add(getClass().getResource("/css/auth.css").toExternalForm());

    primaryStage.setTitle("Garden Simulator - Login");
    primaryStage.setScene(scene);
    primaryStage.setMinWidth(700);
    primaryStage.setMinHeight(650);
    primaryStage.show();
  }

  @Override
  public void stop() {
    if (injector != null) {
      var context =
          injector.getInstance(ua.notion.infrastructure.persistence.PersistenceContext.class);
      if (context != null) {
        context.close();
      }
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
