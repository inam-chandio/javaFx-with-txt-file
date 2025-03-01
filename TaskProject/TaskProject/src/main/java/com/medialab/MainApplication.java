package com.medialab;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDate;
import java.util.List;

// Models
import com.medialab.models.Task;
import com.medialab.models.Category;
import com.medialab.models.Priority;
import com.medialab.models.Reminder;
import com.medialab.models.TaskStatus;
import com.medialab.models.Reminder.ReminderType;

// Service
import com.medialab.services.TaskService;


public class MainApplication extends Application {
    private TaskService taskService;
    private VBox summarySection;
    private TabPane functionSection;
    private TableView<Task> taskTable;
    private TableView<Category> categoryTable;
    private TableView<Priority> priorityTable;
    private TableView<Reminder> reminderTable;

    @Override
    public void start(Stage primaryStage) {
        taskService = TaskService.getInstance();
        taskService.loadData();

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        createSummarySection();
        createFunctionSection();

        root.getChildren().addAll(summarySection, functionSection);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("MediaLab Assistant");
        primaryStage.setScene(scene);

        checkDelayedTasks();
        updateAllTables();
        primaryStage.show();
    }

    private void updateAllTables() {
        updateTaskTable();
        updateCategoryTable();
        updatePriorityTable();
        updateReminderTable();
    }

    private void createSummarySection() {
        summarySection = new VBox(5);
        summarySection.getStyleClass().add("summary-section");
        summarySection.setPadding(new Insets(10));

        Label totalTasksLabel = new Label("Total Tasks: " + taskService.getTotalTasksCount());
        Label completedTasksLabel = new Label("Completed Tasks: " + taskService.getCompletedTasksCount());
        Label delayedTasksLabel = new Label("Delayed Tasks: " + taskService.getDelayedTasksCount());
        Label upcomingTasksLabel = new Label("Tasks Due in 7 Days: " + taskService.getUpcomingTasksCount());

        summarySection.getChildren().addAll(
                totalTasksLabel, completedTasksLabel, delayedTasksLabel, upcomingTasksLabel
        );
    }

    private void createFunctionSection() {
        functionSection = new TabPane();

        Tab tasksTab = new Tab("Tasks");
        tasksTab.setContent(createTasksView());
        tasksTab.setClosable(false);

        Tab categoriesTab = new Tab("Categories");
        categoriesTab.setContent(createCategoriesView());
        categoriesTab.setClosable(false);

        Tab prioritiesTab = new Tab("Priorities");
        prioritiesTab.setContent(createPrioritiesView());
        prioritiesTab.setClosable(false);

        Tab remindersTab = new Tab("Reminders");
        remindersTab.setContent(createRemindersView());
        remindersTab.setClosable(false);

        Tab searchTab = new Tab("Search");
        searchTab.setContent(createSearchView());
        searchTab.setClosable(false);

        functionSection.getTabs().addAll(
                tasksTab, categoriesTab, prioritiesTab, remindersTab, searchTab
        );
    }

    private VBox createTasksView() {
        VBox taskView = new VBox(10);
        taskView.setPadding(new Insets(10));

        Button addTaskBtn = new Button("Add Task");
        taskTable = new TableView<>();

        TableColumn<Task, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Task, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Task, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategory().getName()));

        TableColumn<Task, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPriority().getName()));

        TableColumn<Task, LocalDate> deadlineCol = new TableColumn<>("Deadline");
        deadlineCol.setCellValueFactory(new PropertyValueFactory<>("deadline"));

        TableColumn<Task, TaskStatus> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Task, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(5);

            {
                container.getChildren().addAll(editButton, deleteButton);
                editButton.setOnAction(e -> {
                    Task task = (Task) getTableView().getItems().get(getIndex());
                    showEditTaskDialog(task);
                });
                deleteButton.setOnAction(e -> {
                    Task task = (Task) getTableView().getItems().get(getIndex());
                    confirmAndDeleteTask(task);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        taskTable.getColumns().addAll(
                titleCol, descCol, categoryCol, priorityCol, deadlineCol, statusCol, actionsCol
        );

        addTaskBtn.setOnAction(e -> showAddTaskDialog());
        taskView.getChildren().addAll(addTaskBtn, taskTable);
        return taskView;
    }




    private VBox createCategoriesView() {
        VBox categoryView = new VBox(10);
        categoryView.setPadding(new Insets(10));

        Button addCategoryBtn = new Button("Add Category");
        categoryTable = new TableView<>();

        TableColumn<Category, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Category, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(5);

            {
                container.getChildren().addAll(editButton, deleteButton);
                editButton.setOnAction(e -> {
                    Category category = (Category) getTableView().getItems().get(getIndex());
                    showEditCategoryDialog(category);
                });
                deleteButton.setOnAction(e -> {
                    Category category = (Category) getTableView().getItems().get(getIndex());
                    confirmAndDeleteCategory(category);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        categoryTable.getColumns().addAll(nameCol, actionsCol);
        addCategoryBtn.setOnAction(e -> showAddCategoryDialog());

        categoryView.getChildren().addAll(addCategoryBtn, categoryTable);
        return categoryView;
    }

    private VBox createPrioritiesView() {
        VBox priorityView = new VBox(10);
        priorityView.setPadding(new Insets(10));

        Button addPriorityBtn = new Button("Add Priority");
        priorityTable = new TableView<>();

        TableColumn<Priority, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Priority, Boolean> defaultCol = new TableColumn<>("Default");
        defaultCol.setCellValueFactory(new PropertyValueFactory<>("default"));

        TableColumn<Priority, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(5);

            {
                container.getChildren().addAll(editButton, deleteButton);
                editButton.setOnAction(e -> {
                    Priority priority = (Priority) getTableView().getItems().get(getIndex());
                    if (!priority.isDefault()) {
                        showEditPriorityDialog(priority);
                    }
                });
                deleteButton.setOnAction(e -> {
                    Priority priority = (Priority) getTableView().getItems().get(getIndex());
                    if (!priority.isDefault()) {
                        confirmAndDeletePriority(priority);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        priorityTable.getColumns().addAll(nameCol, defaultCol, actionsCol);
        addPriorityBtn.setOnAction(e -> showAddPriorityDialog());

        priorityView.getChildren().addAll(addPriorityBtn, priorityTable);
        return priorityView;
    }

    private VBox createRemindersView() {
        VBox reminderView = new VBox(10);
        reminderView.setPadding(new Insets(10));

        reminderTable = new TableView<>();

        TableColumn<Reminder, String> taskCol = new TableColumn<>("Task");
        taskCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTask().getTitle()));

        TableColumn<Reminder, ReminderType> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Reminder, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("reminderDate"));

        TableColumn<Reminder, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setOnAction(e -> {
                    Reminder reminder = (Reminder) getTableView().getItems().get(getIndex());
                    confirmAndDeleteReminder(reminder);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        reminderTable.getColumns().addAll(taskCol, typeCol, dateCol, actionsCol);

        Button addReminderBtn = new Button("Add Reminder");
        addReminderBtn.setOnAction(e -> showAddReminderDialog());

        reminderView.getChildren().addAll(addReminderBtn, reminderTable);
        return reminderView;
    }

    private VBox createSearchView() {
        VBox searchView = new VBox(10);
        searchView.setPadding(new Insets(10));

        GridPane searchGrid = new GridPane();
        searchGrid.setHgap(10);
        searchGrid.setVgap(10);
        searchGrid.setPadding(new Insets(10));

        TextField titleSearch = new TextField();
        ComboBox<Category> categorySearch = new ComboBox<>();
        ComboBox<Priority> prioritySearch = new ComboBox<>();
        Button searchBtn = new Button("Search");

        searchGrid.add(new Label("Title:"), 0, 0);
        searchGrid.add(titleSearch, 1, 0);
        searchGrid.add(new Label("Category:"), 0, 1);
        searchGrid.add(categorySearch, 1, 1);
        searchGrid.add(new Label("Priority:"), 0, 2);
        searchGrid.add(prioritySearch, 1, 2);

        TableView<Task> resultTable = new TableView<>();

        TableColumn<Task, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Task, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPriority().getName()));

        TableColumn<Task, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategory().getName()));

        TableColumn<Task, LocalDate> deadlineCol = new TableColumn<>("Deadline");
        deadlineCol.setCellValueFactory(new PropertyValueFactory<>("deadline"));

        resultTable.getColumns().addAll(titleCol, priorityCol, categoryCol, deadlineCol);

        searchBtn.setOnAction(e -> {
            String title = titleSearch.getText().isEmpty() ? null : titleSearch.getText();
            Category category = categorySearch.getValue();
            Priority priority = prioritySearch.getValue();

            List<Task> results = taskService.searchTasks(title, category, priority);
            resultTable.setItems(FXCollections.observableArrayList(results));
        });

        searchView.getChildren().addAll(searchGrid, searchBtn, resultTable);

        // Update the ComboBoxes when the search view is shown
        functionSection.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getText().equals("Search")) {
                categorySearch.setItems(FXCollections.observableArrayList(taskService.getCategories()));
                prioritySearch.setItems(FXCollections.observableArrayList(taskService.getPriorities()));
            }
        });

        return searchView;
    }

    private void showAddTaskDialog() {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Add New Task");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        TextArea descField = new TextArea();
        ComboBox<Category> categoryCombo = new ComboBox<>(
                FXCollections.observableArrayList(taskService.getCategories())
        );
        ComboBox<Priority> priorityCombo = new ComboBox<>(
                FXCollections.observableArrayList(taskService.getPriorities())
        );
        DatePicker deadlinePicker = new DatePicker();
        ComboBox<TaskStatus> statusCombo = new ComboBox<>(
                FXCollections.observableArrayList(TaskStatus.values())
        );

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryCombo, 1, 2);
        grid.add(new Label("Priority:"), 0, 3);
        grid.add(priorityCombo, 1, 3);
        grid.add(new Label("Deadline:"), 0, 4);
        grid.add(deadlinePicker, 1, 4);
        grid.add(new Label("Status:"), 0, 5);
        grid.add(statusCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Task newTask = new Task();
                newTask.setTitle(titleField.getText());
                newTask.setDescription(descField.getText());
                newTask.setCategory(categoryCombo.getValue());
                newTask.setPriority(priorityCombo.getValue());
                newTask.setDeadline(deadlinePicker.getValue());
                newTask.setStatus(statusCombo.getValue());
                return newTask;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(task -> {
            taskService.addTask(task);
            updateTaskTable();
            updateSummarySection();
        });
    }

    private void showAddCategoryDialog() {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle("Add Category");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Category category = new Category();
                category.setName(nameField.getText());
                return category;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(category -> {
            taskService.addCategory(category);
            updateCategoryTable();
        });
    }
    private void showEditTaskDialog(Task task) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField(task.getTitle());
        TextArea descField = new TextArea(task.getDescription());
        ComboBox<Category> categoryCombo = new ComboBox<>(
                FXCollections.observableArrayList(taskService.getCategories())
        );
        categoryCombo.setValue(task.getCategory());

        ComboBox<Priority> priorityCombo = new ComboBox<>(
                FXCollections.observableArrayList(taskService.getPriorities())
        );
        priorityCombo.setValue(task.getPriority());

        DatePicker deadlinePicker = new DatePicker(task.getDeadline());
        ComboBox<TaskStatus> statusCombo = new ComboBox<>(
                FXCollections.observableArrayList(TaskStatus.values())
        );
        statusCombo.setValue(task.getStatus());

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryCombo, 1, 2);
        grid.add(new Label("Priority:"), 0, 3);
        grid.add(priorityCombo, 1, 3);
        grid.add(new Label("Deadline:"), 0, 4);
        grid.add(deadlinePicker, 1, 4);
        grid.add(new Label("Status:"), 0, 5);
        grid.add(statusCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                task.setTitle(titleField.getText());
                task.setDescription(descField.getText());
                task.setCategory(categoryCombo.getValue());
                task.setPriority(priorityCombo.getValue());
                task.setDeadline(deadlinePicker.getValue());
                task.setStatus(statusCombo.getValue());
                return task;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedTask -> {
            taskService.updateTask(updatedTask);
            updateTaskTable();
            updateSummarySection();
        });
    }

    private void confirmAndDeleteTask(Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText("Delete Task");
        alert.setContentText("Are you sure you want to delete this task?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                taskService.deleteTask(task);
                updateTaskTable();
                updateSummarySection();
            }
        });
    }
    private void showEditCategoryDialog(Category category) {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle("Edit Category");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(category.getName());
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                category.setName(nameField.getText());
                return category;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedCategory -> {
            taskService.updateCategory(updatedCategory);
            updateCategoryTable();
        });
    }

    private void confirmAndDeleteCategory(Category category) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Category");
        alert.setHeaderText("Delete Category");
        alert.setContentText("Are you sure you want to delete this category? All associated tasks will be deleted.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                taskService.deleteCategory(category);
                updateCategoryTable();
                updateTaskTable();
                updateSummarySection();
            }
        });
    }
    private void updateSummarySection() {
        summarySection.getChildren().clear();
        summarySection.setPadding(new Insets(10));

        Label totalTasksLabel = new Label("Total Tasks: " + taskService.getTotalTasksCount());
        Label completedTasksLabel = new Label("Completed Tasks: " + taskService.getCompletedTasksCount());
        Label delayedTasksLabel = new Label("Delayed Tasks: " + taskService.getDelayedTasksCount());
        Label upcomingTasksLabel = new Label("Tasks Due in 7 Days: " + taskService.getUpcomingTasksCount());

        summarySection.getChildren().addAll(
                totalTasksLabel, completedTasksLabel, delayedTasksLabel, upcomingTasksLabel
        );
    }
    private void showAddPriorityDialog() {
        Dialog<Priority> dialog = new Dialog<>();
        dialog.setTitle("Add Priority");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Priority priority = new Priority();
                priority.setName(nameField.getText());
                priority.setDefault(false);
                return priority;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(priority -> {
            taskService.addPriority(priority);
            updatePriorityTable();
        });
    }

    private void showEditPriorityDialog(Priority priority) {
        if (priority.isDefault()) {
            showError("Cannot edit default priority");
            return;
        }

        Dialog<Priority> dialog = new Dialog<>();
        dialog.setTitle("Edit Priority");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(priority.getName());
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                priority.setName(nameField.getText());
                return priority;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedPriority -> {
            taskService.updatePriority(updatedPriority);
            updatePriorityTable();
        });
    }

    private void confirmAndDeletePriority(Priority priority) {
        if (priority.isDefault()) {
            showError("Cannot delete default priority");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Priority");
        alert.setHeaderText("Delete Priority");
        alert.setContentText("Are you sure you want to delete this priority? All tasks with this priority will be set to default priority.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                taskService.deletePriority(priority);
                updatePriorityTable();
                updateTaskTable();
            }
        });
    }
    private void updateTaskTable() {
        if (taskTable != null) {
            taskTable.setItems(FXCollections.observableArrayList(taskService.getAllTasks()));
        }
    }

    private void updateCategoryTable() {
        if (categoryTable != null) {
            categoryTable.setItems(FXCollections.observableArrayList(taskService.getCategories()));
        }
    }

    private void updatePriorityTable() {
        if (priorityTable != null) {
            priorityTable.setItems(FXCollections.observableArrayList(taskService.getPriorities()));
        }
    }

    private void updateReminderTable() {
        if (reminderTable != null) {
            reminderTable.setItems(FXCollections.observableArrayList(taskService.getAllReminders()));
        }
    }
    private void showAddReminderDialog() {
        Dialog<Reminder> dialog = new Dialog<>();
        dialog.setTitle("Add Reminder");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Task> taskCombo = new ComboBox<>(
                FXCollections.observableArrayList(taskService.getUncompletedTasks())
        );
        ComboBox<ReminderType> typeCombo = new ComboBox<>(
                FXCollections.observableArrayList(ReminderType.values())
        );
        DatePicker datePicker = new DatePicker();

        grid.add(new Label("Task:"), 0, 0);
        grid.add(taskCombo, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Date:"), 0, 2);
        grid.add(datePicker, 1, 2);

        dialog.getDialogPane().setContent(grid);
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType &&
                    validateReminderInput(taskCombo.getValue(), typeCombo.getValue(), datePicker.getValue())) {
                Reminder reminder = new Reminder();
                reminder.setTask(taskCombo.getValue());
                reminder.setType(typeCombo.getValue());
                reminder.setReminderDate(datePicker.getValue());
                return reminder;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(reminder -> {
            taskService.addReminder(reminder);
            updateReminderTable();
        });
    }

    private void confirmAndDeleteReminder(Reminder reminder) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Reminder");
        alert.setHeaderText("Delete Reminder");
        alert.setContentText("Are you sure you want to delete this reminder?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                taskService.deleteReminder(reminder);
                updateReminderTable();
            }
        });
    }

    private boolean validateReminderInput(Task task, ReminderType type, LocalDate date) {
        if (task == null || type == null || date == null) {
            showError("All fields are required.");
            return false;
        }

        LocalDate taskDeadline = task.getDeadline();
        if (date.isAfter(taskDeadline)) {
            showError("Reminder date cannot be after the task deadline.");
            return false;
        }

        switch (type) {
            case ONE_DAY:
                if (taskDeadline.minusDays(1).isBefore(LocalDate.now())) {
                    showError("One day reminder is not possible for this task's deadline.");
                    return false;
                }
                break;
            case ONE_WEEK:
                if (taskDeadline.minusWeeks(1).isBefore(LocalDate.now())) {
                    showError("One week reminder is not possible for this task's deadline.");
                    return false;
                }
                break;
            case ONE_MONTH:
                if (taskDeadline.minusMonths(1).isBefore(LocalDate.now())) {
                    showError("One month reminder is not possible for this task's deadline.");
                    return false;
                }
                break;
            case CUSTOM:
                if (date.isBefore(LocalDate.now())) {
                    showError("Custom reminder date cannot be in the past.");
                    return false;
                }
                break;
        }
        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void checkDelayedTasks() {
        int delayedCount = taskService.getDelayedTasksCount();
        if (delayedCount > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Delayed Tasks");
            alert.setHeaderText(null);
            alert.setContentText("You have " + delayedCount + " overdue tasks!");
            alert.showAndWait();
        }
    }

    @Override
    public void stop() {
        taskService.saveData();
    }

    public static void main(String[] args) {
        launch(args);
    }
}