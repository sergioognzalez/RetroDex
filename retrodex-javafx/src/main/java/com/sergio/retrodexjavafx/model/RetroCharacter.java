package com.sergio.retrodexjavafx.model;

public class RetroCharacter {
    private final int id;
    private final String name;
    private final String decade;
    private final String category;
    private final String origin;
    private final String description;

    public RetroCharacter(int id,
                          String name,
                          String decade,
                          String category,
                          String origin,
                          String description) {
        this.id = id;
        this.name = name;
        this.decade = decade;
        this.category = category;
        this.origin = origin;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDecade() {
        return decade;
    }

    public String getCategory() {
        return category;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name + " | " + decade + " | " + category;
    }
}
