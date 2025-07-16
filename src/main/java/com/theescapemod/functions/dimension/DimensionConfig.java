package com.theescapemod.functions.dimension;

public class DimensionConfig {
    private String name;
    private int worldBorder;
    private String type;

    public DimensionConfig() {
        // Default constructor for JSON deserialization
    }

    public DimensionConfig(String name, int worldBorder, String type) {
        this.name = name;
        this.worldBorder = worldBorder;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWorldBorder() {
        return worldBorder;
    }

    public void setWorldBorder(int worldBorder) {
        this.worldBorder = worldBorder;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isValid() {
        return name != null && !name.isEmpty() && 
               worldBorder > 0 && 
               type != null && !type.isEmpty();
    }

    @Override
    public String toString() {
        return "DimensionConfig{" +
                "name='" + name + '\'' +
                ", worldBorder=" + worldBorder +
                ", type='" + type + '\'' +
                '}';
    }
}
