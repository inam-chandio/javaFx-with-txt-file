// Reminder.java
package com.medialab.models;

import java.time.LocalDate;

public class Reminder {
    private String id;
    private ReminderType type;
    private LocalDate reminderDate;
    private Task task;

    public enum ReminderType {
        ONE_DAY, ONE_WEEK, ONE_MONTH, CUSTOM
    }

    public Reminder() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    // Getters and setters
    public String getId() { return id; }
    public ReminderType getType() { return type; }
    public void setType(ReminderType type) { this.type = type; }
    public LocalDate getReminderDate() { return reminderDate; }
    public void setReminderDate(LocalDate reminderDate) { this.reminderDate = reminderDate; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
}