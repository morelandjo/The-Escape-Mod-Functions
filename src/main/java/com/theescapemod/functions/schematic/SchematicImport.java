package com.theescapemod.functions.schematic;

import com.google.gson.annotations.SerializedName;

/**
 * Configuration class for a single schematic import.
 * Defines what schematic to load, where to place it, and in which dimension.
 */
public class SchematicImport {
    @SerializedName("filename")
    public String filename;
    
    @SerializedName("dimension")
    public String dimension;
    
    @SerializedName("x")
    public int x;
    
    @SerializedName("y")
    public int y;
    
    @SerializedName("z")
    public int z;
    
    @SerializedName("enabled")
    public boolean enabled = true;
    
    @SerializedName("replace_existing")
    public boolean replaceExisting = true;
    
    @SerializedName("include_entities")
    public boolean includeEntities = false;
    
    public SchematicImport() {}
    
    public SchematicImport(String filename, String dimension, int x, int y, int z) {
        this.filename = filename;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    @Override
    public String toString() {
        return String.format("SchematicImport{filename='%s', dimension='%s', pos=[%d,%d,%d], enabled=%s}", 
                             filename, dimension, x, y, z, enabled);
    }
}
