package ua.notion.presentation.controller;

import com.google.inject.Inject;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import ua.notion.domain.entity.Task;
import ua.notion.domain.entity.User;
import ua.notion.presentation.viewmodel.TaskManagementViewModel;

public class TaskManagementController {

  private static final System.Logger logger =
      System.getLogger(TaskManagementController.class.getName());

  @FXML private Button backButton;
  @FXML private ComboBox<String> statusFilterCombo;
  @FXML private DatePicker dueDatePicker;
  @FXML private Button clearFilterButton;
  @FXML private Button addTaskButton;
  @FXML private TableView<Task> taskTable;
  @FXML private TableColumn<Task, String> idColumn;
  @FXML private TableColumn<Task, String> taskTypeColumn;
  @FXML private TableColumn<Task, String> plantInstanceColumn;
  @FXML private TableColumn<Task, String> dueAtColumn;
  @FXML private TableColumn<Task, String> statusColumn;
  @FXML private TableColumn<Task, Void> actionsColumn;

  private User currentUser;
  private TaskManagementViewModel viewModel;
  private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  @Inject
  public TaskManagementController(TaskManagementViewModel viewModel) {
    this.viewModel = viewModel;
    logger.log(System.Logger.Level.INFO, "TaskManagementController initialized");
  }

  public void initialize() {
    logger.log(System.Logger.Level.INFO, "Initializing TaskManagementController UI");
    taskTable.setItems(viewModel.getTasks());

    setupTableColumns();
    setupFilters();
    setupBindings();
    viewModel.loadTasks();
  }

  private void setupBindings() {
    statusFilterCombo.valueProperty().bindBidirectional(viewModel.statusFilterProperty());
    dueDatePicker.valueProperty().bindBidirectional(viewModel.dueDateFilterProperty());

    viewModel
        .errorMessageProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              if (newVal != null && !newVal.isEmpty()) {
                showError("Error", newVal);
              }
            });

    viewModel
        .successMessageProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              if (newVal != null && !newVal.isEmpty()) {
                showInfo("Success", newVal);
              }
            });
  }

  private void setupTableColumns() {
    idColumn.setCellValueFactory(
        data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));

    taskTypeColumn.setCellValueFactory(
        data -> new SimpleStringProperty(data.getValue().getTaskType()));

    plantInstanceColumn.setCellValueFactory(
        data -> new SimpleStringProperty(String.valueOf(data.getValue().getPlantInstanceId())));

    dueAtColumn.setCellValueFactory(
        data ->
            new SimpleStringProperty(
                data.getValue().getDueAt() != null
                    ? data.getValue().getDueAt().format(dateFormatter)
                    : "N/A"));

    statusColumn.setCellValueFactory(
        data -> new SimpleStringProperty(data.getValue().getIsDone() ? "Done" : "Pending"));

    actionsColumn.setCellFactory(
        param ->
            new TableCell<>() {
              private final Button editButton = new Button("Edit");
              private final Button deleteButton = new Button("Delete");
              private final Button toggleButton = new Button("Toggle");
              private final HBox container = new HBox(5, editButton, toggleButton, deleteButton);

              {
                container.setAlignment(Pos.CENTER);
                editButton.getStyleClass().add("secondary-button");
                deleteButton.getStyleClass().add("danger-button");
                toggleButton.getStyleClass().add("primary-button");

                editButton.setOnAction(
                    event -> {
                      Task task = getTableView().getItems().get(getIndex());
                      handleEditTask(task);
                    });

                deleteButton.setOnAction(
                    event -> {
                      Task task = getTableView().getItems().get(getIndex());
                      handleDeleteTask(task);
                    });

                toggleButton.setOnAction(
                    event -> {
                      Task task = getTableView().getItems().get(getIndex());
                      handleToggleStatus(task);
                    });
              }

              @Override
              protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
              }
            });
  }

  private void setupFilters() {
    statusFilterCombo.setItems(
        javafx.collections.FXCollections.observableArrayList("All Tasks", "Pending", "Done"));
    statusFilterCombo.setValue("All Tasks");

    statusFilterCombo.setOnAction(event -> viewModel.applyFilters());
    dueDatePicker.setOnAction(event -> viewModel.applyFilters());
  }

  @FXML
  private void handleClearFilter() {
    viewModel.clearFilters();
  }

  @FXML
  private void handleAddTask() {
    logger.log(System.Logger.Level.INFO, "User clicked Add Task button");
    showTaskDialog(null);
  }

  private void handleEditTask(Task task) {
    logger.log(
        System.Logger.Level.INFO, "User clicked Edit Task button for task ID: {0}", task.getId());
    showTaskDialog(task);
  }

  private void handleToggleStatus(Task task) {
    logger.log(
        System.Logger.Level.INFO, "User clicked Toggle Status for task ID: {0}", task.getId());
    viewModel.toggleTaskStatus(task);
  }

  private void handleDeleteTask(Task task) {
    logger.log(
        System.Logger.Level.INFO, "User clicked Delete Task button for task ID: {0}", task.getId());
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Confirm Delete");
    confirm.setHeaderText("Delete Task");
    confirm.setContentText("Are you sure you want to delete this task?");

    Optional<ButtonType> result = confirm.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      logger.log(System.Logger.Level.INFO, "User confirmed deletion of task ID: {0}", task.getId());
      viewModel.deleteTask(task.getId());
    } else {
      logger.log(System.Logger.Level.INFO, "User cancelled deletion");
    }
  }

  private void showTaskDialog(Task task) {
    logger.log(
        System.Logger.Level.INFO,
        "Opening task dialog for: {0}",
        task == null ? "new task" : "task ID " + task.getId());
    Dialog<Task> dialog = new Dialog<>();
    dialog.setTitle(task == null ? "Add Task" : "Edit Task");
    dialog.setHeaderText(task == null ? "Create a new task" : "Edit task details");

    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    ComboBox<String> taskTypeCombo = new ComboBox<>();
    taskTypeCombo.setItems(
        javafx.collections.FXCollections.observableArrayList(
            "water", "fertilize", "harvest", "prune"));
    if (task != null) taskTypeCombo.setValue(task.getTaskType());
    TextField plantInstanceField =
        new TextField(task != null ? String.valueOf(task.getPlantInstanceId()) : "");
    DatePicker dueDateField =
        new DatePicker(
            task != null && task.getDueAt() != null
                ? task.getDueAt().toLocalDate()
                : java.time.LocalDate.now());
    CheckBox isDoneCheck = new CheckBox();
    if (task != null) isDoneCheck.setSelected(task.getIsDone());

    grid.add(new Label("Task Type:"), 0, 0);
    grid.add(taskTypeCombo, 1, 0);
    grid.add(new Label("Plant Instance ID:"), 0, 1);
    grid.add(plantInstanceField, 1, 1);
    grid.add(new Label("Due Date (required):"), 0, 2);
    grid.add(dueDateField, 1, 2);
    grid.add(new Label("Is Done:"), 0, 3);
    grid.add(isDoneCheck, 1, 3);

    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(
        dialogButton -> {
          if (dialogButton == saveButtonType) {
            try {
              if (dueDateField.getValue() == null) {
                logger.log(System.Logger.Level.ERROR, "Validation failed: Due Date is null");
                showError("Error", "Due Date is required");
                return null;
              }
              if (taskTypeCombo.getValue() == null || taskTypeCombo.getValue().isEmpty()) {
                logger.log(System.Logger.Level.ERROR, "Validation failed: Task Type is null");
                showError("Error", "Task Type is required");
                return null;
              }
              Task result = task != null ? task : new Task();
              result.setTaskType(taskTypeCombo.getValue());
              result.setPlantInstanceId(Integer.parseInt(plantInstanceField.getText()));
              result.setDueAt(dueDateField.getValue().atStartOfDay());
              result.setIsDone(isDoneCheck.isSelected());
              logger.log(
                  System.Logger.Level.INFO,
                  "Task dialog data: type={0}, plantInstanceId={1}, dueAt={2}, isDone={3}",
                  result.getTaskType(),
                  result.getPlantInstanceId(),
                  result.getDueAt(),
                  result.getIsDone());
              return result;
            } catch (NumberFormatException e) {
              logger.log(
                  System.Logger.Level.ERROR, "Validation failed: Invalid Plant Instance ID", e);
              showError("Error", "Plant Instance ID must be a number");
              return null;
            }
          }
          return null;
        });

    Optional<Task> result = dialog.showAndWait();
    result.ifPresent(
        taskData -> {
          if (task == null) {
            logger.log(System.Logger.Level.INFO, "Creating new task from dialog");
            viewModel.createTask(
                taskData.getPlantInstanceId(),
                taskData.getTaskType(),
                taskData.getDueAt(),
                taskData.getIsDone());
          } else {
            logger.log(System.Logger.Level.INFO, "Updating existing task from dialog");
            viewModel.updateTask(taskData);
          }
        });
  }

  @FXML
  private void handleBack() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
      Parent root = loader.load();

      MenuController controller = loader.getController();
      controller.setCurrentUser(currentUser);

      Stage stage = (Stage) backButton.getScene().getWindow();
      Scene scene = new Scene(root, 800, 600);
      stage.setScene(scene);
    } catch (IOException e) {
      showError("Error", "Failed to return to menu: " + e.getMessage());
    }
  }

  public void setCurrentUser(User user) {
    this.currentUser = user;
  }

  private void showInfo(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
