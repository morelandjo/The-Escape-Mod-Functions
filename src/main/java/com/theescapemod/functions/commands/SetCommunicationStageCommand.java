package com.theescapemod.functions.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.theescapemod.functions.communication.GameStageChecker;
import com.theescapemod.functions.communication.SceneManager;
import com.theescapemod.functions.item.ModItems;
import com.theescapemod.functions.network.ToastPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Command to set communication stage without any output text.
 */
public class SetCommunicationStageCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("communication_stage")
            .then(Commands.argument("stage", IntegerArgumentType.integer(0))
                .executes(SetCommunicationStageCommand::setCommunicationStage)));
    }
    
    private static int setCommunicationStage(CommandContext<CommandSourceStack> context) {
        int stage = IntegerArgumentType.getInteger(context, "stage");
        
        if (context.getSource().getEntity() instanceof ServerPlayer player) {
            // Check if player has Astral Communicator
            if (!player.getInventory().contains(new ItemStack(ModItems.ASTRAL_COMMUNICATOR.get()))) {
                return 1; // Silent failure if no communicator
            }
            
            // Store the communication stage in player's persistent data
            player.getPersistentData().putInt("temf_communication_stage", stage);
            
            // Force check for new scenes immediately
            GameStageChecker.forceCheckPlayer(player);
            
            // Show toast notification if there's a pending scene
            if (SceneManager.hasPendingScene(player.getUUID())) {
                // Send toast notification to client
                PacketDistributor.sendToPlayer(player, new ToastPacket(
                    "Incoming Call", 
                    "Check the Astral Communicator"
                ));
            }
        }
        
        // Return success without any output text
        return 1;
    }
    
    /**
     * Get the current communication stage for a player.
     */
    public static int getCommunicationStage(ServerPlayer player) {
        return player.getPersistentData().getInt("temf_communication_stage");
    }
}
