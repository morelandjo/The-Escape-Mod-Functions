package com.theescapemod.functions.communication;

import com.mojang.logging.LogUtils;
import com.theescapemod.functions.commands.SetCommunicationStageCommand;
import com.theescapemod.functions.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Monitors communication stage changes and triggers communication scenes.
 * Checks every 30 seconds for players with the Astral Communicator.
 * Also checks when players log in to ensure immediate scene triggering.
 */
@EventBusSubscriber
public class GameStageChecker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, Integer> lastKnownStages = new HashMap<>();
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Check for scenes immediately when player logs in
            if (hasAstralCommunicator(player)) {
                int currentStage = SetCommunicationStageCommand.getCommunicationStage(player);
                lastKnownStages.put(player.getUUID(), currentStage);
                SceneManager.checkForNewScenes(player.getUUID());
                
                // Send action bar notification if there's a pending scene
                if (SceneManager.hasPendingScene(player.getUUID())) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "§6§lIncoming Transmission - Check your Astral Communicator"), true);
                }
                
                LOGGER.info("Checked communication scenes for player {} on login", player.getGameProfile().getName());
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter >= 600) { // Check every 30 seconds (20 ticks/sec * 30)
            tickCounter = 0;
            checkPlayersForStageChanges();
        }
    }

    /**
     * Check all players for communication stage changes and trigger scenes if needed.
     */
    private static void checkPlayersForStageChanges() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (hasAstralCommunicator(player)) {
                int currentStage = SetCommunicationStageCommand.getCommunicationStage(player);
                Integer lastStage = lastKnownStages.get(player.getUUID());
                
                if (lastStage == null || currentStage != lastStage) {
                    lastKnownStages.put(player.getUUID(), currentStage);
                    SceneManager.checkForNewScenes(player.getUUID());
                    
                    // Send action bar notification if there's a pending scene
                    if (SceneManager.hasPendingScene(player.getUUID())) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                            "§6§lIncoming Transmission - Check your Astral Communicator"), true);
                    }
                }
            }
        }
    }

    /**
     * Check if a player has the Astral Communicator in their inventory.
     */
    private static boolean hasAstralCommunicator(ServerPlayer player) {
        return player.getInventory().contains(new ItemStack(ModItems.ASTRAL_COMMUNICATOR.get()));
    }

    /**
     * Force check a specific player for stage changes (useful for testing).
     */
    public static void forceCheckPlayer(ServerPlayer player) {
        if (hasAstralCommunicator(player)) {
            int currentStage = SetCommunicationStageCommand.getCommunicationStage(player);
            lastKnownStages.put(player.getUUID(), currentStage);
            SceneManager.checkForNewScenes(player.getUUID());
        }
    }
}
