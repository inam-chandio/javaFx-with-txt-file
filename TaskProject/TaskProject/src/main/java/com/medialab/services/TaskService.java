package com.medialab.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medialab.models.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TaskService {
    private static TaskService instance;
    private List<Task> tasks;
    private List<Category> categories;
    private List<Priority> priorities;
    private List<Reminder> reminders;
    private final String DATA_DIR = "src/main/resources/medialab";
    private final ObjectMapper objectMapper;

    private TaskService() {
        tasks = new ArrayList<>();
        categories = new ArrayList<>();
        priorities = new ArrayList<>();
        reminders = new ArrayList<>();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        createDataDirIfNotExists();
        initializeDefaultPriority();
    }

    public static TaskService getInstance() {
        if (instance == null) {
            instance = new TaskService();
        }
        return instance;
    }

    private void createDataDirIfNotExists() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeDefaultPriority() {
        if (priorities.isEmpty()) {
            Priority defaultPriority = new Priority();
            defaultPriority.setName("Default");
            defaultPriority.setDefault(true);
            priorities.add(defaultPriority);
        }
    }

    // Task Operations
    public void addTask(Task task) {
        tasks.add(task);
    }

    public void updateTask(Task task) {
        int index = tasks.indexOf(task);
        if (index != -1) {
            tasks.set(index, task);
        }
    }

    public void deleteTask(Task task) {
        tasks.remove(task);
        reminders.removeIf(reminder -> reminder.getTask().equals(task));
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    public List<Task> getUncompletedTasks() {
        return tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .collect(Collectors.toList());
    }

    public int getTotalTasksCount() {
        return tasks.size();
    }

    public int getCompletedTasksCount() {
        return (int) tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .count();
    }

    public int getDelayedTasksCount() {
        return (int) tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DELAYED)
                .count();
    }

    public int getUpcomingTasksCount() {
        LocalDate nextWeek = LocalDate.now().plusDays(7);
        return (int) tasks.stream()
                .filter(task -> !task.getDeadline().isAfter(nextWeek)
                        && task.getStatus() != TaskStatus.COMPLETED)
                .count();
    }

    // Category Operations
    public void addCategory(Category category) {
        categories.add(category);
    }

    public void updateCategory(Category category) {
        int index = categories.indexOf(category);
        if (index != -1) {
            categories.set(index, category);
        }
    }

    public void deleteCategory(Category category) {
        categories.remove(category);
        List<Task> tasksToRemove = tasks.stream()
                .filter(task -> task.getCategory().equals(category))
                .collect(Collectors.toList());
        tasks.removeAll(tasksToRemove);
        reminders.removeIf(reminder -> tasksToRemove.contains(reminder.getTask()));
    }

    public List<Category> getCategories() {
        return new ArrayList<>(categories);
    }

    // Priority Operations
    public void addPriority(Priority priority) {
        priorities.add(priority);
    }

    public void updatePriority(Priority priority) {
        int index = priorities.indexOf(priority);
        if (index != -1) {
            priorities.set(index, priority);
        }
    }

    public void deletePriority(Priority priority) {
        if (!priority.isDefault()) {
            priorities.remove(priority);
            Priority defaultPriority = getDefaultPriority();
            tasks.stream()
                    .filter(task -> task.getPriority().equals(priority))
                    .forEach(task -> task.setPriority(defaultPriority));
        }
    }

    public List<Priority> getPriorities() {
        return new ArrayList<>(priorities);
    }

    public Priority getDefaultPriority() {
        return priorities.stream()
                .filter(Priority::isDefault)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No default priority found"));
    }

    // Reminder Operations
    public void addReminder(Reminder reminder) {
        reminders.add(reminder);
    }

    public void deleteReminder(Reminder reminder) {
        reminders.remove(reminder);
    }

    public List<Reminder> getAllReminders() {
        return new ArrayList<>(reminders);
    }

    // Search Operations
    public List<Task> searchTasks(String title, Category category, Priority priority) {
        return tasks.stream()
                .filter(task ->
                        (title == null || task.getTitle().toLowerCase().contains(title.toLowerCase())) &&
                                (category == null || task.getCategory().equals(category)) &&
                                (priority == null || task.getPriority().equals(priority)))
                .collect(Collectors.toList());
    }

    // Data Load/Save Operations
    public void loadData() {
        try {
            loadCategories();
            loadPriorities();
            loadTasks();
            loadReminders();
            updateDelayedTasks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() throws IOException {
        Path path = Paths.get(DATA_DIR, "tasks.json");
        if (Files.exists(path)) {
            JsonNode rootNode = objectMapper.readTree(path.toFile());
            JsonNode tasksNode = rootNode.get("tasks");
            tasks = new ArrayList<>(Arrays.asList(objectMapper.treeToValue(tasksNode, Task[].class)));

            // Link tasks with categories and priorities
            for (Task task : tasks) {
                task.setCategory(findCategoryById(task.getCategoryId()));
                task.setPriority(findPriorityById(task.getPriorityId()));
            }
        }
    }

    private void loadCategories() throws IOException {
        Path path = Paths.get(DATA_DIR, "categories.json");
        if (Files.exists(path)) {
            JsonNode rootNode = objectMapper.readTree(path.toFile());
            JsonNode categoriesNode = rootNode.get("categories");
            categories = new ArrayList<>(Arrays.asList(objectMapper.treeToValue(categoriesNode, Category[].class)));
        }
    }

    private void loadPriorities() throws IOException {
        Path path = Paths.get(DATA_DIR, "priorities.json");
        if (Files.exists(path)) {
            try {
                JsonNode rootNode = objectMapper.readTree(path.toFile());
                JsonNode prioritiesNode = rootNode.get("priorities");
                priorities = new ArrayList<>(Arrays.asList(objectMapper.treeToValue(prioritiesNode, Priority[].class)));
            } catch (Exception e) {
                e.printStackTrace();
                priorities = new ArrayList<>();
            }
        }
        initializeDefaultPriority();
    }

    private void loadReminders() throws IOException {
        Path path = Paths.get(DATA_DIR, "reminders.json");
        if (Files.exists(path)) {
            JsonNode rootNode = objectMapper.readTree(path.toFile());
            JsonNode remindersNode = rootNode.get("reminders");
            if (remindersNode != null) {
                reminders = new ArrayList<>(Arrays.asList(objectMapper.treeToValue(remindersNode, Reminder[].class)));
            }
        }
    }

    private Category findCategoryById(String id) {
        return categories.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private Priority findPriorityById(String id) {
        return priorities.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseGet(this::getDefaultPriority);
    }

    public void saveData() {
        try {
            saveTasks();
            saveCategories();
            savePriorities();
            saveReminders();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveTasks() throws IOException {
        Path path = Paths.get(DATA_DIR, "tasks.json");
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.set("tasks", objectMapper.valueToTree(tasks));
        objectMapper.writeValue(path.toFile(), rootNode);
    }

    private void saveCategories() throws IOException {
        Path path = Paths.get(DATA_DIR, "categories.json");
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.set("categories", objectMapper.valueToTree(categories));
        objectMapper.writeValue(path.toFile(), rootNode);
    }

    private void savePriorities() throws IOException {
        Path path = Paths.get(DATA_DIR, "priorities.json");
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.set("priorities", objectMapper.valueToTree(priorities));
        objectMapper.writeValue(path.toFile(), rootNode);
    }

    private void saveReminders() throws IOException {
        Path path = Paths.get(DATA_DIR, "reminders.json");
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.set("reminders", objectMapper.valueToTree(reminders));
        objectMapper.writeValue(path.toFile(), rootNode);
    }

    private void updateDelayedTasks() {
        LocalDate today = LocalDate.now();
        tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED
                        && task.getDeadline().isBefore(today))
                .forEach(task -> task.setStatus(TaskStatus.DELAYED));
    }
}