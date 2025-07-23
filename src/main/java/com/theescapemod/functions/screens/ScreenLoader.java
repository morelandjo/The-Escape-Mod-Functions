package com.theescapemod.functions.screens;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Handles loading and saving screen configurations from config/temf/screens.json
 */
public class ScreenLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ScreenConfig screenConfig = new ScreenConfig();
    
    public static void init() {
        LOGGER.info("Initializing screen display system...");
        loadScreenConfig();
    }
    
    /**
     * Load screen configuration from config/temf/screens.json
     */
    public static void loadScreenConfig() {
        File configDir = new File("config/temf");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        File screenFile = new File(configDir, "screens.json");
        
        if (!screenFile.exists()) {
            createDefaultConfig(screenFile);
            return;
        }
        
        try (FileReader reader = new FileReader(screenFile)) {
            screenConfig = GSON.fromJson(reader, ScreenConfig.class);
            if (screenConfig == null) {
                screenConfig = new ScreenConfig();
                LOGGER.warn("screens.json was empty or invalid, using default configuration");
            } else {
                LOGGER.info("Loaded {} screen configurations", screenConfig.screens.size());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load screens.json: {}", e.getMessage());
            screenConfig = new ScreenConfig();
        }
    }
    
    /**
     * Create a default screens.json file with example content
     */
    private static void createDefaultConfig(File screenFile) {
        ScreenConfig defaultConfig = new ScreenConfig();
        
        // Add example screen configurations
        ScreenConfig.ScreenData screen1 = new ScreenConfig.ScreenData();
        screen1.title = "Welcome Screen";
        screen1.text = "Welcome to the escape mod! This is an example screen that can be triggered by right-clicking a block with the NBT tag 'screendisp:screen1'. You can configure multiple screens in the config/temf/screens.json file.";
        screen1.text_color = 0xFFFFFF;
        screen1.background_color = 0xE0000000;
        screen1.center_text = false;
        defaultConfig.screens.put("screen1", screen1);
        
        ScreenConfig.ScreenData screen2 = new ScreenConfig.ScreenData();
        screen2.title = "Information Terminal";
        screen2.text = "This is another example screen. You can have as many screens as you want, each with different text content. The text will automatically wrap and display with a typewriter effect. If the text is too long, an arrow will appear to continue to the next part.";
        screen2.text_color = 0x00FF00; // Green text
        screen2.background_color = 0xE0001100;
        screen2.center_text = true;
        defaultConfig.screens.put("screen2", screen2);
        
        try (FileWriter writer = new FileWriter(screenFile)) {
            GSON.toJson(defaultConfig, writer);
            LOGGER.info("Created default screens.json with example configurations");
        } catch (IOException e) {
            LOGGER.error("Failed to create default screens.json: {}", e.getMessage());
        }
        
        screenConfig = defaultConfig;
    }
    
    /**
     * Get the current screen configuration
     */
    public static ScreenConfig getScreenConfig() {
        return screenConfig;
    }
    
    /**
     * Reload the screen configuration from disk
     */
    public static void reloadConfig() {
        LOGGER.info("Reloading screen configurations...");
        loadScreenConfig();
    }
}
