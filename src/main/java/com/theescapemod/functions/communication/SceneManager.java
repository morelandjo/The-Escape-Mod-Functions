package com.theescapemod.functions.communication;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.*;

/**
 * Manages communication scenes and tracks player progress.
 * Handles scene completion tracking and triggering new scenes.
 */
public class SceneManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // Track completed scenes per player
    private static final Map<UUID, Set<Integer>> completedScenes = new HashMap<>();
    // Track pending scenes waiting to be viewed
    private static final Map<UUID, Integer> pendingScenes = new HashMap<>();

    /**
     * Check if any new scenes should be triggered for a player based on game stage changes.
     */
    public static void checkForNewScenes(UUID playerUUID) {
        CommunicationConfig config = CommunicationLoader.getConfig();
        if (config == null) return;

        Set<Integer> playerCompletedScenes = completedScenes.getOrDefault(playerUUID, new HashSet<>());
        
        // Check each scene to see if it should be triggered
        for (CommunicationConfig.Scene scene : config.scenes) {
            // Skip if scene already completed
            if (playerCompletedScenes.contains(scene.scene_number)) {
                continue;
            }
            
            // Skip if this exact scene is already pending
            Integer currentPending = pendingScenes.get(playerUUID);
            if (currentPending != null && currentPending.equals(scene.scene_number)) {
                continue;
            }
            
            // Check if player meets the requirements for this scene
            if (shouldTriggerScene(playerUUID, scene)) {
                pendingScenes.put(playerUUID, scene.scene_number);
                LOGGER.info("Triggered scene {} for player {}", scene.scene_number, playerUUID);
                break; // Only trigger one scene at a time
            }
        }
    }

    /**
     * Check if a scene should be triggered for a player.
     * Checks if the player's communication stage meets the scene requirements.
     */
    private static boolean shouldTriggerScene(UUID playerUUID, CommunicationConfig.Scene scene) {
        // Get the player's current communication stage
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;
        
        net.minecraft.server.level.ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player == null) return false;
        
        int playerStage = com.theescapemod.functions.commands.SetCommunicationStageCommand.getCommunicationStage(player);
        
        // Trigger scene if player's stage meets or exceeds the required stage
        return playerStage >= scene.required_game_stage;
    }

    /**
     * Mark a scene as completed for a player.
     */
    public static void markSceneComplete(UUID playerUUID, int sceneNumber) {
        completedScenes.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(sceneNumber);
        pendingScenes.remove(playerUUID);
        LOGGER.info("Scene {} marked complete for player {}", sceneNumber, playerUUID);
        
        // Immediately check for new scenes after completing this one
        checkForNewScenes(playerUUID);
    }

    /**
     * Get the pending scene number for a player, if any.
     */
    public static Integer getPendingScene(UUID playerUUID) {
        return pendingScenes.get(playerUUID);
    }

    /**
     * Check if a player has a pending scene.
     */
    public static boolean hasPendingScene(UUID playerUUID) {
        return pendingScenes.containsKey(playerUUID);
    }

    /**
     * Check if a player has completed a specific scene.
     */
    public static boolean hasCompletedScene(UUID playerUUID, int sceneNumber) {
        Set<Integer> completed = completedScenes.getOrDefault(playerUUID, new HashSet<>());
        return completed.contains(sceneNumber);
    }

    /**
     * Get all completed scenes for a player.
     */
    public static Set<Integer> getCompletedScenes(UUID playerUUID) {
        return new HashSet<>(completedScenes.getOrDefault(playerUUID, new HashSet<>()));
    }

    /**
     * Clear a pending scene without marking it complete (for when player closes GUI early).
     */
    public static void clearPendingScene(UUID playerUUID) {
        pendingScenes.remove(playerUUID);
    }
}
