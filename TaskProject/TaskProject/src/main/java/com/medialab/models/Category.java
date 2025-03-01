package com.medialab.models;

public class Category {
    private String id;
    private String name;

    public Category() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name;  // This will display just the name in ComboBox
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id.equals(category.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}