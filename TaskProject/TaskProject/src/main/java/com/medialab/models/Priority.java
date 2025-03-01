package com.medialab.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Priority {
    private String id;
    private String name;

    @JsonProperty("isDefault")
    private boolean isDefault;

    public Priority() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @JsonProperty("isDefault")
    public boolean isDefault() { return isDefault; }

    @JsonProperty("isDefault")
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    @Override
    public String toString() {
        return name;  // This will display just the name in ComboBox
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Priority priority = (Priority) o;
        return id.equals(priority.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}