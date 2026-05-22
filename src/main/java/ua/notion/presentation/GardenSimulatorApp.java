package ua.notion.presentation;

import atlantafx.base.theme.PrimerLight;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import ua.notion.domain.service.auth.AuthenticationService;
import ua.notion.infrastructure.config.PersistenceModule;
import ua.notion.infrastructure.config.ServiceModule;
import ua.notion.infrastructure.persistence.PersistenceContext;
import ua.notion.presentation.controller.auth.LoginController;
import ua.notion.presentation.ui.SceneCoordinator;

public class GardenSimulatorApp extends Application {

  private Injector injector;

  @Override
  public void init() {
    injector = Guice.createInjector(new PersistenceModule(), new ServiceModule());
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

    SceneCoordinator coordinator = new SceneCoordinator(primaryStage);
    SceneCoordinator.attach(primaryStage, coordinator);

    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
    AuthenticationService authService = injector.getInstance(AuthenticationService.class);
    LoginController controller = new LoginController(authService);
    loader.setController(controller);
    Parent root = loader.load();

    coordinator.setContent(root);
    coordinator.addStylesheet("/css/auth.css");

    primaryStage.setTitle("Garden Simulator");
    primaryStage.show();
  }

  @Override
  public void stop() {
    if (injector != null) {
      PersistenceContext context = injector.getInstance(PersistenceContext.class);
      if (context != null) {
        context.close();
      }
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
