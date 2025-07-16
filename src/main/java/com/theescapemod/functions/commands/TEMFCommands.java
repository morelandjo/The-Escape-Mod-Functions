package com.theescapemod.functions.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.theescapemod.functions.dimension.DimensionConfig;
import com.theescapemod.functions.dimension.DimensionManager;
import com.theescapemod.functions.world.WorldBorderManager;
import com.theescapemod.functions.world.BarrierManager;
import com.theescapemod.functions.schematic.SchematicManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Map;

public class TEMFCommands {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("temf")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reload")
                        .executes(TEMFCommands::reloadConfigs))
                .then(Commands.literal("list")
                        .executes(TEMFCommands::listDimensions))
                .then(Commands.literal("borders")
                        .executes(TEMFCommands::applyWorldBorders))
                .then(Commands.literal("barriers")
                        .executes(TEMFCommands::placeBarriers)
                        .then(Commands.literal("remove")
                                .executes(TEMFCommands::removeBarriers))
                        .then(Commands.literal("refresh")
                                .executes(TEMFCommands::refreshBarriers)))
                .then(Commands.literal("check")
                        .then(Commands.argument("dimension", StringArgumentType.string())
                                .executes(TEMFCommands::checkDimension)))
                .then(Commands.literal("pos")
                        .executes(TEMFCommands::checkPosition))
                .then(Commands.literal("schematics")
                        .executes(TEMFCommands::listSchematics)
                        .then(Commands.literal("reload")
                                .executes(TEMFCommands::reloadSchematics))
                        .then(Commands.literal("import")
                                .executes(TEMFCommands::importSchematics)))
        );
    }
    
    private static int reloadConfigs(CommandContext<CommandSourceStack> context) {
        DimensionManager.reloadConfigs();
        context.getSource().sendSuccess(() -> Component.literal("Reloaded TEMF dimension configurations and regenerated datapack"), true);
        return 1;
    }
    
    private static int listDimensions(CommandContext<CommandSourceStack> context) {
        Map<String, DimensionConfig> dimensions = DimensionManager.getLoadedDimensions();
        
        if (dimensions.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("No dimensions loaded"), false);
            return 0;
        }
        
        context.getSource().sendSuccess(() -> Component.literal("Loaded dimensions:"), false);
        for (DimensionConfig config : dimensions.values()) {
            String status = WorldBorderManager.dimensionExists(context.getSource().getServer(), config.getName()) 
                ? "§a✓ Active" : "§c✗ Not loaded";
            context.getSource().sendSuccess(() -> Component.literal("- " + config.getName() + 
                    " (border: " + config.getWorldBorder() + ", type: " + config.getType() + ") " + status), false);
        }
        
        return dimensions.size();
    }
    
    private static int applyWorldBorders(CommandContext<CommandSourceStack> context) {
        Map<String, DimensionConfig> dimensions = DimensionManager.getLoadedDimensions();
        WorldBorderManager.applyWorldBorders(context.getSource().getServer(), dimensions);
        context.getSource().sendSuccess(() -> Component.literal("Applied world borders to all configured dimensions"), true);
        return dimensions.size();
    }
    
    private static int checkDimension(CommandContext<CommandSourceStack> context) {
        String dimensionName = StringArgumentType.getString(context, "dimension");
        DimensionConfig config = DimensionManager.getDimensionConfig(dimensionName);
        
        if (config == null) {
            context.getSource().sendFailure(Component.literal("Dimension '" + dimensionName + "' not found in configurations"));
            return 0;
        }
        
        boolean exists = WorldBorderManager.dimensionExists(context.getSource().getServer(), dimensionName);
        double currentBorder = WorldBorderManager.getWorldBorderSize(context.getSource().getServer(), dimensionName);
        
        context.getSource().sendSuccess(() -> Component.literal("Dimension: " + dimensionName), false);
        context.getSource().sendSuccess(() -> Component.literal("- Configured border: " + config.getWorldBorder()), false);
        context.getSource().sendSuccess(() -> Component.literal("- Type: " + config.getType()), false);
        context.getSource().sendSuccess(() -> Component.literal("- Status: " + (exists ? "§aLoaded" : "§cNot loaded")), false);
        if (exists) {
            context.getSource().sendSuccess(() -> Component.literal("- Current border: " + (int)currentBorder), false);
            context.getSource().sendSuccess(() -> Component.literal("- Teleport: /execute in theescapemodfunctions:" + dimensionName + " run tp @s ~ ~ ~"), false);
        }
        
        return 1;
    }
    
    private static int checkPosition(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (source.getEntity() == null) {
            source.sendFailure(Component.literal("This command can only be run by a player"));
            return 0;
        }
        
        double x = source.getPosition().x;
        double z = source.getPosition().z;
        double distanceFromCenter = Math.sqrt(x * x + z * z);
        
        String fullDimensionName = source.getLevel().dimension().location().toString();
        String dimensionName = source.getLevel().dimension().location().getPath();
        
        final String displayName;
        if (fullDimensionName.startsWith("theescapemodfunctions:")) {
            displayName = dimensionName;
        } else {
            displayName = fullDimensionName;
        }
        
        source.sendSuccess(() -> Component.literal("Position Check:"), false);
        source.sendSuccess(() -> Component.literal("- Coordinates: " + (int)x + ", " + (int)z), false);
        source.sendSuccess(() -> Component.literal("- Distance from center: " + (int)distanceFromCenter), false);
        source.sendSuccess(() -> Component.literal("- Dimension: " + displayName), false);
        
        // Check world border if in a TEMF dimension
        DimensionConfig config = DimensionManager.getDimensionConfig(dimensionName);
        if (config != null) {
            double currentBorder = WorldBorderManager.getWorldBorderSize(source.getServer(), dimensionName);
            source.sendSuccess(() -> Component.literal("- World border radius: " + (int)currentBorder), false);
            
            if (distanceFromCenter > currentBorder) {
                source.sendSuccess(() -> Component.literal("§c- You are OUTSIDE the world border!"), false);
            } else {
                double remaining = currentBorder - distanceFromCenter;
                source.sendSuccess(() -> Component.literal("§a- You are inside the border (" + (int)remaining + " blocks remaining)"), false);
            }
        }
        
        return 1;
    }
    
    private static int placeBarriers(CommandContext<CommandSourceStack> context) {
        Map<String, DimensionConfig> dimensions = DimensionManager.getLoadedDimensions();
        int totalPlaced = 0;
        
        for (DimensionConfig config : dimensions.values()) {
            if (WorldBorderManager.dimensionExists(context.getSource().getServer(), config.getName())) {
                int placed = BarrierManager.placeBarriers(context.getSource().getServer(), config);
                totalPlaced += placed;
                final int placedCount = placed; // Make variable effectively final for lambda
                context.getSource().sendSuccess(() -> Component.literal("Placed " + placedCount + " barriers in " + config.getName()), false);
            }
        }
        
        final int finalTotal = totalPlaced; // Make variable effectively final for lambda
        context.getSource().sendSuccess(() -> Component.literal("Total barriers placed: " + finalTotal), true);
        return totalPlaced;
    }
    
    private static int removeBarriers(CommandContext<CommandSourceStack> context) {
        Map<String, DimensionConfig> dimensions = DimensionManager.getLoadedDimensions();
        int totalRemoved = 0;
        
        for (DimensionConfig config : dimensions.values()) {
            if (WorldBorderManager.dimensionExists(context.getSource().getServer(), config.getName())) {
                int removed = BarrierManager.removeBarriers(context.getSource().getServer(), config);
                totalRemoved += removed;
                final int removedCount = removed; // Make variable effectively final for lambda
                context.getSource().sendSuccess(() -> Component.literal("Removed " + removedCount + " barriers from " + config.getName()), false);
            }
        }
        
        final int finalTotal = totalRemoved; // Make variable effectively final for lambda
        context.getSource().sendSuccess(() -> Component.literal("Total barriers removed: " + finalTotal), true);
        return totalRemoved;
    }
    
    private static int refreshBarriers(CommandContext<CommandSourceStack> context) {
        Map<String, DimensionConfig> dimensions = DimensionManager.getLoadedDimensions();
        int totalProcessed = 0;
        
        for (DimensionConfig config : dimensions.values()) {
            if (WorldBorderManager.dimensionExists(context.getSource().getServer(), config.getName())) {
                // Refresh will handle both removal and placement internally
                int placed = BarrierManager.refreshBarriers(context.getSource().getServer(), config);
                totalProcessed += placed;
                final int placedCount = placed; // Make variable effectively final for lambda
                context.getSource().sendSuccess(() -> Component.literal("Refreshed barriers in " + config.getName() + 
                        " (placed: " + placedCount + " blocks)"), false);
            }
        }
        
        context.getSource().sendSuccess(() -> Component.literal("Refreshed barriers for all dimensions"), true);
        return totalProcessed;
    }
    
    private static int listSchematics(CommandContext<CommandSourceStack> context) {
        var schematics = SchematicManager.getAvailableSchematics();
        
        if (schematics.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("No schematic files found in config/temf/schematics/"), false);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("Found " + schematics.size() + " schematic files:"), false);
            for (String schematic : schematics) {
                context.getSource().sendSuccess(() -> Component.literal("  " + schematic), false);
            }
        }
        
        var config = SchematicManager.getConfig();
        if (config != null) {
            var enabledImports = config.getEnabledImports();
            context.getSource().sendSuccess(() -> Component.literal("Configured imports: " + enabledImports.size() + " enabled"), false);
        }
        
        return schematics.size();
    }
    
    private static int reloadSchematics(CommandContext<CommandSourceStack> context) {
        SchematicManager.reloadConfig();
        context.getSource().sendSuccess(() -> Component.literal("Reloaded schematic configuration"), true);
        return 1;
    }
    
    private static int importSchematics(CommandContext<CommandSourceStack> context) {
        SchematicManager.executeImports(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.literal("Executed schematic imports"), true);
        return 1;
    }
}
