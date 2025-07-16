package com.theescapemod.functions.schematic;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

/**
 * Configuration file format for schematic imports.
 * This file should be placed at config/temf/schematics.json
 */
public class SchematicConfig {
    @SerializedName("imports")
    public List<SchematicImport> imports = new ArrayList<>();
    
    @SerializedName("enabled")
    public boolean enabled = true;
    
    @SerializedName("version")
    public String version = "1.0";
    
    public SchematicConfig() {}
    
    public void addImport(SchematicImport schematicImport) {
        imports.add(schematicImport);
    }
    
    public List<SchematicImport> getEnabledImports() {
        return imports.stream()
                .filter(imp -> imp.enabled)
                .toList();
    }
}
