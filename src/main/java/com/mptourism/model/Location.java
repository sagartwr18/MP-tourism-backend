package com.mptourism.model;

public class Location {
    
    private int id;
    private String name;
    private String city;
    private int categoryId;

    public Location() {}

    public Location(int id, String name, String city, int categoryId) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.categoryId = categoryId;
    }

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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
}
