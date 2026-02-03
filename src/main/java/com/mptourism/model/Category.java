package com.mptourism.model;

import java.util.Map;
import java.util.List;

public class Category {

    private int id;
    private String name;
    private String description;

    // private List<Map<String, Object>> category;
    private Map<String, Object> updates;

    // REQUIRED for Jackson (VERY IMPORTANT)
    public Category() {
    }

    // Parameterized constructor (for manual creation)
    public Category(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Getters & Setters (REQUIRED)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    } 

    public Map<String, Object> getUpdates() {
        return updates;
    }

    public void setUpdates(Map<String, Object> updates) {
        this.updates = updates;
    }
}
