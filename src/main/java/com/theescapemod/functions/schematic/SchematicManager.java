package com.theescapemod.functions.schematic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main manager for schematic import functionality.
 * Handles loading config, reading schematics, and placing them in worlds.
 */
public class SchematicManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private static final String CONFIG_DIR = "config/temf";
    private static final String SCHEMATICS_DIR = "config/temf/schematics";
    private static final String CONFIG_FILE = "config/temf/schematics.json";
    
    private static SchematicConfig config;
    
    /**
     * Initialize the schematic system - create directories and default config if needed.
     */
    public static void init() {
        LOGGER.info("Initializing schematic import system");
        
        try {
            // Create directories
            File configDir = new File(CONFIG_DIR);
            File schematicsDir = new File(SCHEMATICS_DIR);
            
            if (!configDir.exists()) {
                configDir.mkdirs();
                LOGGER.info("Created config directory: {}", configDir.getAbsolutePath());
            }
            
            if (!schematicsDir.exists()) {
                schematicsDir.mkdirs();
                LOGGER.info("Created schematics directory: {}", schematicsDir.getAbsolutePath());
            }
            
            // Load or create config
            loadConfig();
            
        } catch (Exception e) {
            LOGGER.error("Failed to initialize schematic system: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Load the schematic configuration file.
     */
    public static void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        
        if (!configFile.exists()) {
            LOGGER.info("Creating default schematic config at {}", configFile.getAbsolutePath());
            createDefaultConfig(configFile);
        }
        
        try (FileReader reader = new FileReader(configFile)) {
            config = GSON.fromJson(reader, SchematicConfig.class);
            if (config == null) {
                config = new SchematicConfig();
            }
            LOGGER.info("Loaded schematic config with {} imports", config.imports.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load schematic config: {}", e.getMessage(), e);
            config = new SchematicConfig();
        }
    }
    
    /**
     * Create a default configuration file with examples.
     */
    private static void createDefaultConfig(File configFile) {
        SchematicConfig defaultConfig = new SchematicConfig();
        
        // Add example imports
        defaultConfig.addImport(new SchematicImport("example_house.schem", "minecraft:overworld", 0, 64, 0));
        defaultConfig.addImport(new SchematicImport("spawn_platform.schem", "theescapemodfunctions:void_spawn", 0, 100, 0));
        
        // Disable the examples by default
        for (SchematicImport imp : defaultConfig.imports) {
            imp.enabled = false;
        }
        
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(defaultConfig, writer);
            LOGGER.info("Created default schematic config");
        } catch (IOException e) {
            LOGGER.error("Failed to create default config: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Execute all enabled schematic imports.
     */
    public static void executeImports(MinecraftServer server) {
        LOGGER.info("=== SCHEMATIC IMPORT SYSTEM STARTING ===");
        LOGGER.info("Server instance: {}", server != null ? "Valid" : "NULL");
        LOGGER.info("Working directory: {}", System.getProperty("user.dir"));
        LOGGER.info("Config file path: {}", new File(CONFIG_FILE).getAbsolutePath());
        LOGGER.info("Schematics directory: {}", new File(SCHEMATICS_DIR).getAbsolutePath());
        
        if (config == null) {
            LOGGER.error("Schematic config is null! Trying to reload...");
            loadConfig();
            if (config == null) {
                LOGGER.error("Still null after reload - aborting schematic imports");
                return;
            }
        }
        
        LOGGER.info("Config loaded successfully. Global enabled: {}", config.enabled);
        LOGGER.info("Config version: {}", config.version);
        
        if (!config.enabled) {
            LOGGER.info("Schematic imports are disabled in global config - skipping");
            return;
        }
        
        List<SchematicImport> enabledImports = config.getEnabledImports();
        LOGGER.info("Total imports in config: {}, Enabled imports: {}", config.imports.size(), enabledImports.size());
        
        // Log details about each import
        for (int i = 0; i < config.imports.size(); i++) {
            SchematicImport imp = config.imports.get(i);
            LOGGER.info("Import {}: {} -> {} at ({},{},{}) [enabled: {}]", 
                       i + 1, imp.filename, imp.dimension, imp.x, imp.y, imp.z, imp.enabled);
        }
        
        if (enabledImports.isEmpty()) {
            LOGGER.info("No enabled schematic imports found - nothing to do");
            return;
        }
        
        LOGGER.info("Starting execution of {} schematic imports", enabledImports.size());
        
        for (int i = 0; i < enabledImports.size(); i++) {
            SchematicImport schematicImport = enabledImports.get(i);
            LOGGER.info("");
            LOGGER.info(">>> Processing import {}/{}: {} <<<", i + 1, enabledImports.size(), schematicImport.filename);
            executeImport(server, schematicImport);
        }
        
        LOGGER.info("");
        LOGGER.info("=== SCHEMATIC IMPORT SYSTEM COMPLETE ===");
    }
    
    /**
     * Execute a single schematic import.
     */
    private static void executeImport(MinecraftServer server, SchematicImport schematicImport) {
        try {
            LOGGER.info("--- Processing import: {} ---", schematicImport.filename);
            LOGGER.info("Target dimension: {}", schematicImport.dimension);
            LOGGER.info("Target position: ({}, {}, {})", schematicImport.x, schematicImport.y, schematicImport.z);
            
            // Find the schematic file
            File schematicFile = findSchematicFile(schematicImport.filename);
            if (schematicFile == null) {
                LOGGER.error("Schematic file not found: {}", schematicImport.filename);
                LOGGER.error("Checked in directory: {}", new File(SCHEMATICS_DIR).getAbsolutePath());
                LOGGER.info("Available files:");
                File schematicsDir = new File(SCHEMATICS_DIR);
                if (schematicsDir.exists()) {
                    File[] files = schematicsDir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            LOGGER.info("  - {}", file.getName());
                        }
                    }
                }
                return;
            }
            
            LOGGER.info("Found schematic file: {}", schematicFile.getAbsolutePath());
            LOGGER.info("File size: {} bytes", schematicFile.length());
            
            // Get the target dimension
            ResourceLocation dimensionId = ResourceLocation.parse(schematicImport.dimension);
            LOGGER.info("Parsed dimension ID: {}", dimensionId);
            
            ServerLevel level = null;
            LOGGER.info("Available dimensions:");
            for (var key : server.levelKeys()) {
                LOGGER.info("  - {}", key.location());
                if (key.location().equals(dimensionId)) {
                    level = server.getLevel(key);
                    LOGGER.info("    ^ Found matching dimension!");
                }
            }
            
            if (level == null) {
                LOGGER.error("Dimension not found: {}", schematicImport.dimension);
                return;
            }
            
            LOGGER.info("Target dimension found: {}", level.dimension().location());
            
            // Read the schematic
            LOGGER.info("Reading schematic file...");
            SimpleSchematic schematic = SchematicReader.readSchematic(schematicFile);
            LOGGER.info("Schematic loaded: {}x{}x{} with {} blocks", 
                       schematic.width, schematic.height, schematic.length, 
                       schematic.getAllBlocks().size());
            
            // Paste the schematic
            BlockPos targetPos = new BlockPos(schematicImport.x, schematicImport.y, schematicImport.z);
            LOGGER.info("Pasting schematic at world position: {}", targetPos);
            
            boolean success = SchematicPaster.pasteSchematic(
                    level, 
                    schematic, 
                    targetPos, 
                    schematicImport.replaceExisting, 
                    schematicImport.includeEntities
            );
            
            if (success) {
                LOGGER.info("✓ Successfully imported schematic '{}' to {} at {}", 
                           schematicImport.filename, schematicImport.dimension, targetPos);
            } else {
                LOGGER.error("✗ Failed to import schematic '{}'", schematicImport.filename);
            }
            
        } catch (Exception e) {
            LOGGER.error("Error executing schematic import '{}': {}", schematicImport.filename, e.getMessage(), e);
        }
    }
    
    /**
     * Find a schematic file by name in the schematics directory.
     */
    private static File findSchematicFile(String filename) {
        File schematicsDir = new File(SCHEMATICS_DIR);
        
        // Try the exact filename first
        File schematicFile = new File(schematicsDir, filename);
        if (schematicFile.exists()) {
            return schematicFile;
        }
        
        // Try common extensions if not specified
        if (!filename.contains(".")) {
            String[] extensions = {".schem", ".schematic", ".nbt"};
            for (String ext : extensions) {
                schematicFile = new File(schematicsDir, filename + ext);
                if (schematicFile.exists()) {
                    return schematicFile;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get a list of all schematic files in the schematics directory.
     */
    public static List<String> getAvailableSchematics() {
        List<String> schematics = new ArrayList<>();
        File schematicsDir = new File(SCHEMATICS_DIR);
        
        if (schematicsDir.exists() && schematicsDir.isDirectory()) {
            File[] files = schematicsDir.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".schem") || 
                name.toLowerCase().endsWith(".schematic") ||
                name.toLowerCase().endsWith(".nbt"));
            
            if (files != null) {
                for (File file : files) {
                    schematics.add(file.getName());
                }
            }
        }
        
        return schematics;
    }
    
    /**
     * Reload the configuration from disk.
     */
    public static void reloadConfig() {
        LOGGER.info("Reloading schematic configuration");
        loadConfig();
    }
    
    /**
     * Get the current configuration.
     */
    public static SchematicConfig getConfig() {
        return config;
    }
}
