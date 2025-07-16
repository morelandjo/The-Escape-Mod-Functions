package com.theescapemod.functions.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import com.theescapemod.functions.TheEscapeModFunctions;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Utility class for checking if custom dimensions are loaded and available.
 * This replaces the old datapack-based dimension checking.
 */
public class DimensionChecker {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // Dimension keys for our custom dimensions
    public static final ResourceKey<Level> VOID_SPAWN = ResourceKey.create(Registries.DIMENSION, 
            ResourceLocation.fromNamespaceAndPath(TheEscapeModFunctions.MODID, "void_spawn"));
    
    /**
     * Check if a dimension is loaded on the server.
     */
    public static boolean isDimensionLoaded(MinecraftServer server, ResourceKey<Level> dimensionKey) {
        if (server == null) {
            return false;
        }
        
        ServerLevel level = server.getLevel(dimensionKey);
        boolean isLoaded = level != null;
        
        LOGGER.info("Dimension {} - {}", 
                dimensionKey.location(), 
                isLoaded ? "✓ Loaded" : "✗ Not loaded");
        
        return isLoaded;
    }
    
    /**
     * Check all custom dimensions and log their status.
     */
    public static void checkAllDimensions(MinecraftServer server) {
        LOGGER.info("=== Dimension Status Check ===");
        isDimensionLoaded(server, VOID_SPAWN);
        LOGGER.info("==============================");
    }
}
