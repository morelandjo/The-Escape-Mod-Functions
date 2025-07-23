package com.theescapemod.functions.communication;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Loader for the communication configuration file.
 * Handles reading and creating the communication.json file.
 */
public class CommunicationLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "config/temf/communication.json";
    
    private static CommunicationConfig config;

    /**
     * Initialize the communication system by loading or creating the config file.
     */
    public static void init() {
        File configFile = new File(CONFIG_FILE);
        
        if (!configFile.exists()) {
            LOGGER.info("Communication config not found, creating default at: {}", configFile.getAbsolutePath());
            createDefaultConfig(configFile);
        }
        
        loadConfig();
    }

    /**
     * Load the communication configuration from disk.
     */
    private static void loadConfig() {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            config = GSON.fromJson(reader, CommunicationConfig.class);
            if (config == null) {
                config = new CommunicationConfig();
            }
            LOGGER.info("Loaded communication config with {} scenes", config.scenes.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load communication config: {}", e.getMessage(), e);
            config = new CommunicationConfig();
        }
    }

    /**
     * Create a default configuration file with example scenes.
     */
    private static void createDefaultConfig(File configFile) {
        CommunicationConfig defaultConfig = new CommunicationConfig();
        
        // Create example scene 1
        CommunicationConfig.Scene scene1 = new CommunicationConfig.Scene();
        scene1.scene_number = 1;
        scene1.required_game_stage = 1;
        scene1.messages = new ArrayList<>();
        
        CommunicationConfig.Message msg1 = new CommunicationConfig.Message();
        msg1.type = "standard";
        msg1.name = "Space Man";
        msg1.message = "Hello, can you hear me? This is Control.";
        msg1.name_side = "left";
        msg1.image = "space_man";
        scene1.messages.add(msg1);
        
        CommunicationConfig.Message msg2 = new CommunicationConfig.Message();
        msg2.type = "standard";
        msg2.name = "%player%";
        msg2.message = "Yes, who is this?";
        msg2.name_side = "right";
        msg2.image = "%player%";
        scene1.messages.add(msg2);
        
        CommunicationConfig.Message msg3 = new CommunicationConfig.Message();
        msg3.type = "standard";
        msg3.name = "Space Man";
        msg3.message = "I've been monitoring your progress, %player%. You're not alone out there.";
        msg3.name_side = "left";
        msg3.image = "space_man";
        scene1.messages.add(msg3);
        
        defaultConfig.scenes.add(scene1);
        
        // Create example scene 2
        CommunicationConfig.Scene scene2 = new CommunicationConfig.Scene();
        scene2.scene_number = 2;
        scene2.required_game_stage = 2;
        scene2.messages = new ArrayList<>();
        
        CommunicationConfig.Message q1 = new CommunicationConfig.Message();
        q1.type = "question";
        q1.name = "Space Man";
        q1.message = "I see you've made it further, %player%. What's your next move?";
        q1.name_side = "left";
        q1.image = "space_man";
        q1.options = new ArrayList<>();
        q1.options.add("Continue exploring");
        q1.options.add("Ask for help");
        q1.options.add("Stay cautious");
        scene2.messages.add(q1);
        
        CommunicationConfig.Message a1 = new CommunicationConfig.Message();
        a1.type = "answer";
        a1.name = "%player%";
        a1.name_side = "right";
        a1.image = "%player%";
        a1.answers = new ArrayList<>();
        a1.answers.add("I'll keep exploring this place.");
        a1.answers.add("I need guidance, this place is confusing.");
        a1.answers.add("I'm being careful, something feels wrong.");
        scene2.messages.add(a1);
        
        CommunicationConfig.Message r1 = new CommunicationConfig.Message();
        r1.type = "reply";
        r1.name = "Space Man";
        r1.name_side = "left";
        r1.image = "space_man";
        r1.replies = new ArrayList<>();
        r1.replies.add("Good %player%, but be prepared for what's ahead.");
        r1.replies.add("I understand %player%. Follow my coordinates carefully.");
        r1.replies.add("Your instincts are right, %player%. Trust them.");
        scene2.messages.add(r1);
        
        defaultConfig.scenes.add(scene2);
        
        try {
            configFile.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(configFile);
            GSON.toJson(defaultConfig, writer);
            writer.close();
            LOGGER.info("Created default communication config");
        } catch (IOException e) {
            LOGGER.error("Failed to create default communication config: {}", e.getMessage(), e);
        }
    }

    /**
     * Get the current communication configuration.
     */
    public static CommunicationConfig getConfig() {
        return config;
    }

    /**
     * Reload the configuration from disk.
     */
    public static void reloadConfig() {
        LOGGER.info("Reloading communication configuration");
        loadConfig();
    }
}
