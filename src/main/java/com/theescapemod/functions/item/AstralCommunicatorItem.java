package com.theescapemod.functions.item;

import com.theescapemod.functions.communication.SceneManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The Astral Communicator item.
 * When used, opens the communication interface if there are pending messages.
 */
public class AstralCommunicatorItem extends Item {
    
    public AstralCommunicatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        if (level.isClientSide() && player instanceof net.minecraft.client.player.LocalPlayer localPlayer) {
            // Client side - open GUI
            Integer pendingScene = SceneManager.getPendingScene(localPlayer.getUUID());
            if (pendingScene != null) {
                openCommunicatorGUI(pendingScene);
            } else {
                // Check for new scenes
                SceneManager.checkForNewScenes(localPlayer.getUUID());
                pendingScene = SceneManager.getPendingScene(localPlayer.getUUID());
                if (pendingScene != null) {
                    openCommunicatorGUI(pendingScene);
                } else {
                    localPlayer.sendSystemMessage(Component.literal("No incoming transmissions."));
                }
            }
        }
        
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    /**
     * Open the communicator GUI on the client side.
     */
    private void openCommunicatorGUI(int sceneNumber) {
        var config = com.theescapemod.functions.communication.CommunicationLoader.getConfig();
        if (config != null) {
            var scene = config.getScene(sceneNumber);
            if (scene != null) {
                net.minecraft.client.Minecraft.getInstance().setScreen(
                    new com.theescapemod.functions.client.gui.CommunicatorScreen(scene));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§7A mysterious device for interstellar communication"));
        tooltipComponents.add(Component.literal("§7Right-click to check for incoming transmissions"));
    }
    
    /**
     * Create an item property function that determines if the communicator should show as active.
     * Returns 1.0f if there's a pending scene, 0.0f otherwise.
     */
    public static ClampedItemPropertyFunction createActivePropertyFunction() {
        return (ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) -> {
            if (level != null && entity instanceof Player player) {
                // Check if there's a pending scene for this player
                Integer pendingScene = SceneManager.getPendingScene(player.getUUID());
                return pendingScene != null ? 1.0f : 0.0f;
            }
            return 0.0f;
        };
    }
}
