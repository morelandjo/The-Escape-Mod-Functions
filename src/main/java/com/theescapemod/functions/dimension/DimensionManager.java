package com.theescapemod.functions.dimension;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.theescapemod.functions.config.TEMFConfig;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class DimensionManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, DimensionConfig> LOADED_DIMENSIONS = new HashMap<>();
    
    private static Path configDir;
    private static Path temfDir;
    private static Path dimensionsDir;

    public static void init() {
        configDir = FMLPaths.CONFIGDIR.get();
        temfDir = configDir.resolve("temf");
        dimensionsDir = temfDir.resolve("dimensions");
        
        createDirectories();
        createExampleDimensions();
    }

    private static void createDirectories() {
        try {
            Files.createDirectories(dimensionsDir);
            LOGGER.info("Created TEMF directories at: {}", temfDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create TEMF directories", e);
        }
    }

    private static void createExampleDimensions() {
        Path examplePath = dimensionsDir.resolve("void_spawn.json");
        
        if (!Files.exists(examplePath)) {
            DimensionConfig example = new DimensionConfig("void_spawn", 100, "void");
            try {
                String json = GSON.toJson(example);
                Files.writeString(examplePath, json, StandardOpenOption.CREATE);
                LOGGER.info("Created example dimension config: {}", examplePath);
            } catch (IOException e) {
                LOGGER.error("Failed to create example dimension config", e);
            }
        }
    }

    public static void loadDimensionConfigs() {
        if (!TEMFConfig.ENABLE_DIMENSION_LOADING.get()) {
            LOGGER.info("Dimension loading is disabled in config");
            return;
        }

        LOADED_DIMENSIONS.clear();
        
        try {
            if (!Files.exists(dimensionsDir)) {
                LOGGER.warn("Dimensions directory does not exist: {}", dimensionsDir);
                return;
            }

            Files.walk(dimensionsDir, 1)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(DimensionManager::loadDimensionConfig);
                    
            LOGGER.info("Loaded {} dimension configurations", LOADED_DIMENSIONS.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load dimension configurations", e);
        }
    }

    private static void loadDimensionConfig(Path configPath) {
        try {
            String json = Files.readString(configPath);
            DimensionConfig config = GSON.fromJson(json, DimensionConfig.class);
            
            if (config == null) {
                LOGGER.warn("Failed to parse dimension config: {}", configPath);
                return;
            }
            
            if (!config.isValid()) {
                LOGGER.warn("Invalid dimension config: {} - {}", configPath, config);
                return;
            }
            
            // Validate dimension type
            if (!"void".equals(config.getType())) {
                LOGGER.warn("Unsupported dimension type '{}' in config: {}", config.getType(), configPath);
                return;
            }
            
            LOADED_DIMENSIONS.put(config.getName(), config);
            LOGGER.info("Loaded dimension config: {}", config);
            
        } catch (Exception e) {
            LOGGER.error("Failed to load dimension config from: {}", configPath, e);
        }
    }

    public static Map<String, DimensionConfig> getLoadedDimensions() {
        return new HashMap<>(LOADED_DIMENSIONS);
    }

    public static DimensionConfig getDimensionConfig(String name) {
        return LOADED_DIMENSIONS.get(name);
    }

    public static boolean hasDimension(String name) {
        return LOADED_DIMENSIONS.containsKey(name);
    }

    public static Path getDimensionsDirectory() {
        return dimensionsDir;
    }

    public static void reloadConfigs() {
        LOGGER.info("Reloading dimension configurations...");
        loadDimensionConfigs();
    }
}
