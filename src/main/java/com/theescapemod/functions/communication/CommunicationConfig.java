package com.theescapemod.functions.communication;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration structure for the Astral Communicator system.
 * Defines scenes and messages for story progression.
 */
public class CommunicationConfig {
    public List<Scene> scenes = new ArrayList<>();

    public static class Scene {
        public int scene_number;
        public int required_game_stage;
        public List<Message> messages = new ArrayList<>();
    }

    public static class Message {
        public String type; // "standard", "question", "answer", "reply"
        public String name;
        public String message;
        public String name_side; // "left" or "right" - determines position of name box
        public String image; // Image identifier for speaker portrait
        public List<String> options; // For question type
        public List<String> answers; // For answer type (corresponds to options)
        public List<String> replies; // For reply type (corresponds to selected answer)
    }

    /**
     * Get a scene by its number.
     */
    public Scene getScene(int sceneNumber) {
        return scenes.stream()
                .filter(scene -> scene.scene_number == sceneNumber)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all scenes that require a specific game stage.
     */
    public List<Scene> getScenesForGameStage(int gameStage) {
        return scenes.stream()
                .filter(scene -> scene.required_game_stage == gameStage)
                .toList();
    }
}
