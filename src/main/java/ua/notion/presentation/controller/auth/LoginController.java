package ua.notion.presentation.controller.auth;

import atlantafx.base.theme.PrimerLight;
import com.google.inject.Inject;
import java.util.Optional;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ua.notion.domain.entity.User;
import ua.notion.domain.service.auth.AuthenticationService;
import ua.notion.presentation.viewmodel.LoginViewModel;

public class LoginController {
  @FXML private TextField usernameField;
  @FXML private PasswordField passwordField;
  @FXML private Button loginButton;
  @FXML private Button registerButton;
  @FXML private Label errorLabel;

  private final LoginViewModel viewModel;
  private Stage stage;
  private Runnable onLoginSuccess;

  @Inject
  public LoginController(AuthenticationService authService) {
    this.viewModel = new LoginViewModel(authService);
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public void setOnLoginSuccess(Runnable onLoginSuccess) {
    this.onLoginSuccess = onLoginSuccess;
  }

  @FXML
  private void initialize() {
    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

    // Bind view to viewmodel
    usernameField.textProperty().bindBidirectional(viewModel.usernameProperty());
    passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
    errorLabel.textProperty().bind(viewModel.errorMessageProperty());
    errorLabel.visibleProperty().bind(viewModel.hasErrorProperty());

    loginButton.setOnAction(event -> handleLogin());
    registerButton.setOnAction(event -> handleRegister());

    usernameField.setOnAction(event -> handleLogin());
    passwordField.setOnAction(event -> handleLogin());
  }

  private void handleLogin() {
    Optional<User> userOpt = viewModel.login();

    if (userOpt.isPresent()) {
      User user = userOpt.get();
      showSuccess("Welcome, " + user.getUsername() + "!");

      if (onLoginSuccess != null) {
        onLoginSuccess.run();
      }
    }
  }

  private void handleRegister() {
    try {
      javafx.fxml.FXMLLoader loader =
          new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/register.fxml"));

      RegisterController registerController =
          new RegisterController(
              new AuthenticationService(
                  com.google.inject.Guice.createInjector(
                          new ua.notion.infrastructure.config.PersistenceModule(),
                          new ua.notion.infrastructure.config.ServiceModule())
                      .getInstance(ua.notion.infrastructure.persistence.PersistenceContext.class)));
      registerController.setStage(stage);
      registerController.setOnRegisterSuccess(
          () -> {
            try {
              javafx.fxml.FXMLLoader loginLoader =
                  new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/login.fxml"));
              loginLoader.setController(this);
              javafx.scene.Parent root = loginLoader.load();
              stage.getScene().setRoot(root);
            } catch (Exception e) {
              viewModel.errorMessageProperty().set("Failed to return to login: " + e.getMessage());
              viewModel.hasErrorProperty().set(true);
            }
          });

      loader.setController(registerController);
      javafx.scene.Parent root = loader.load();
      stage.getScene().setRoot(root);
    } catch (Exception e) {
      viewModel.errorMessageProperty().set("Failed to open registration: " + e.getMessage());
      viewModel.hasErrorProperty().set(true);
    }
  }

  private void showSuccess(String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Success");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
