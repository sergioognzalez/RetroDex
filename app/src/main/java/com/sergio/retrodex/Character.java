package com.sergio.retrodex;

public class Character {
    private int id;
    private String name;
    private String description;
    private String decade;
    private String category;
    private String origin;
    private String imagePath;

    public Character() {}

    public Character(int id, String name, String description,
                     String decade, String category, String origin, String imagePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.decade = decade;
        this.category = category;
        this.origin = origin;
        this.imagePath = imagePath;
    }

    // Constructor sin id (para inserciones nuevas)
    public Character(String name, String description,
                     String decade, String category, String origin, String imagePath) {
        this.name = name;
        this.description = description;
        this.decade = decade;
        this.category = category;
        this.origin = origin;
        this.imagePath = imagePath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDecade() { return decade; }
    public void setDecade(String decade) { this.decade = decade; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    @Override
    public String toString() {
        return name + " | " + decade + " | " + category;
    }
}
