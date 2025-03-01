package com.medialab.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Task {
    private String id;
    private String title;
    private String description;
    private String categoryId;  // For JSON mapping
    private String priorityId;  // For JSON mapping

    @JsonIgnore
    private Category category;
    @JsonIgnore
    private Priority priority;

    private LocalDate deadline;
    private TaskStatus status;
    private List<Reminder> reminders;

    public Task() {
        this.id = java.util.UUID.randomUUID().toString();
        this.status = TaskStatus.OPEN;
        this.reminders = new ArrayList<>();
    }

    // Getters and setters
    public String getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getPriorityId() { return priorityId; }
    public void setPriorityId(String priorityId) { this.priorityId = priorityId; }

    @JsonIgnore
    public Category getCategory() { return category; }
    public void setCategory(Category category) {
        this.category = category;
        if (category != null) {
            this.categoryId = category.getId();
        }
    }

    @JsonIgnore
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) {
        this.priority = priority;
        if (priority != null) {
            this.priorityId = priority.getId();
        }
    }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public List<Reminder> getReminders() { return reminders; }
    public void setReminders(List<Reminder> reminders) { this.reminders = reminders; }

    public void addReminder(Reminder reminder) {
        if (status != TaskStatus.COMPLETED) {
            reminders.add(reminder);
        }
    }

    public void removeReminder(Reminder reminder) {
        reminders.remove(reminder);
    }

    public void checkDeadline() {
        if (status != TaskStatus.COMPLETED && deadline.isBefore(LocalDate.now())) {
            status = TaskStatus.DELAYED;
        }
    }
}