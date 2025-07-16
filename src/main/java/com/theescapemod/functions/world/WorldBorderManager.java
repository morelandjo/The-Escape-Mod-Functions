package com.theescapemod.functions.world;

import com.mojang.logging.LogUtils;
import com.theescapemod.functions.TheEscapeModFunctions;
import com.theescapemod.functions.dimension.DimensionConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Manages world borders for TEMF dimensions.
 * This is much simpler than full dimension registration - just sets borders.
 */
public class WorldBorderManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Applies world borders to all configured dimensions.
     * Called when server starts or configs are reloaded.
     */
    public static void applyWorldBorders(MinecraftServer server, Map<String, DimensionConfig> configs) {
        for (DimensionConfig config : configs.values()) {
            applyWorldBorder(server, config);
        }
    }
    
    /**
     * Applies world border to a specific dimension.
     */
    public static void applyWorldBorder(MinecraftServer server, DimensionConfig config) {
        ResourceLocation dimensionLocation = ResourceLocation.fromNamespaceAndPath(
            TheEscapeModFunctions.MODID, config.getName());
        ResourceKey<Level> dimensionKey = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION, dimensionLocation);
        
        ServerLevel level = server.getLevel(dimensionKey);
        if (level != null) {
            WorldBorder border = level.getWorldBorder();
            double configValue = config.getWorldBorder();
            // Treat config value as diameter directly (for testing)
            border.setSize(configValue);
            border.setCenter(0.0, 0.0);
            
            LOGGER.info("Set world border for dimension '{}' - size: {} (radius: {})", 
                config.getName(), configValue, configValue / 2.0);
        } else {
            LOGGER.warn("Could not find dimension '{}' to set world border. Make sure the datapack is loaded.", config.getName());
        }
    }
    
    /**
     * Gets the current world border size for a dimension.
     */
    public static double getWorldBorderSize(MinecraftServer server, String dimensionName) {
        ResourceLocation dimensionLocation = ResourceLocation.fromNamespaceAndPath(
            TheEscapeModFunctions.MODID, dimensionName);
        ResourceKey<Level> dimensionKey = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION, dimensionLocation);
        
        ServerLevel level = server.getLevel(dimensionKey);
        if (level != null) {
            return level.getWorldBorder().getSize() / 2.0; // Convert diameter back to radius
        }
        return -1;
    }
    
    /**
     * Checks if a dimension exists on the server.
     */
    public static boolean dimensionExists(MinecraftServer server, String dimensionName) {
        ResourceLocation dimensionLocation = ResourceLocation.fromNamespaceAndPath(
            TheEscapeModFunctions.MODID, dimensionName);
        ResourceKey<Level> dimensionKey = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION, dimensionLocation);
        
        return server.getLevel(dimensionKey) != null;
    }
}
