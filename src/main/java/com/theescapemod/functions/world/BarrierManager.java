package com.theescapemod.functions.world;

import com.mojang.logging.LogUtils;
import com.theescapemod.functions.TheEscapeModFunctions;
import com.theescapemod.functions.dimension.DimensionConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Manages invisible barrier blocks around dimension perimeters.
 * Creates a physical wall that players cannot pass through.
 */
public class BarrierManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // Height range for barriers (covers most of the world height)
    private static final int MIN_BARRIER_Y = -64;
    private static final int MAX_BARRIER_Y = 320;
    
    /**
     * Places barriers around all configured dimensions.
     */
    public static void placeAllBarriers(MinecraftServer server, Map<String, DimensionConfig> configs) {
        for (DimensionConfig config : configs.values()) {
            placeBarriers(server, config);
        }
    }
    
    /**
     * Places barriers around a specific dimension.
     * @return number of blocks placed
     */
    public static int placeBarriers(MinecraftServer server, DimensionConfig config) {
        ResourceLocation dimensionLocation = ResourceLocation.fromNamespaceAndPath(
            TheEscapeModFunctions.MODID, config.getName());
        ResourceKey<Level> dimensionKey = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION, dimensionLocation);
        
        ServerLevel level = server.getLevel(dimensionKey);
        if (level != null) {
            int radius = config.getWorldBorder();
            int blocksPlaced = placeBarrierWall(level, radius);
            LOGGER.info("Placed {} barrier blocks for dimension '{}' at radius {}", blocksPlaced, config.getName(), radius);
            return blocksPlaced;
        } else {
            LOGGER.warn("Could not find dimension '{}' to place barriers", config.getName());
            return 0;
        }
    }
    
    /**
     * Places a square wall of barrier blocks around the specified radius.
     * @return number of blocks placed
     */
    private static int placeBarrierWall(ServerLevel level, int radius) {
        BlockState barrierState = Blocks.BARRIER.defaultBlockState();
        int blocksPlaced = 0;
        
        // Place barriers in a square pattern around the perimeter
        for (int y = MIN_BARRIER_Y; y <= MAX_BARRIER_Y; y++) {
            // North and South walls (x = -radius and x = +radius)
            for (int z = -radius; z <= radius; z++) {
                // North wall
                BlockPos northPos = new BlockPos(-radius, y, z);
                level.setBlock(northPos, barrierState, 3);
                blocksPlaced++;
                
                // South wall
                BlockPos southPos = new BlockPos(radius, y, z);
                level.setBlock(southPos, barrierState, 3);
                blocksPlaced++;
            }
            
            // East and West walls (z = -radius and z = +radius)
            // Skip corners to avoid double-placing blocks
            for (int x = -radius + 1; x < radius; x++) {
                // West wall
                BlockPos westPos = new BlockPos(x, y, -radius);
                level.setBlock(westPos, barrierState, 3);
                blocksPlaced++;
                
                // East wall
                BlockPos eastPos = new BlockPos(x, y, radius);
                level.setBlock(eastPos, barrierState, 3);
                blocksPlaced++;
            }
        }
        
        LOGGER.debug("Placed {} barrier blocks for radius {}", blocksPlaced, radius);
        return blocksPlaced;
    }
    
    /**
     * Removes barriers around a specific dimension.
     * @return number of blocks removed
     */
    public static int removeBarriers(MinecraftServer server, DimensionConfig config) {
        ResourceLocation dimensionLocation = ResourceLocation.fromNamespaceAndPath(
            TheEscapeModFunctions.MODID, config.getName());
        ResourceKey<Level> dimensionKey = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION, dimensionLocation);
        
        ServerLevel level = server.getLevel(dimensionKey);
        if (level != null) {
            int radius = config.getWorldBorder();
            int blocksRemoved = removeBarrierWall(level, radius);
            LOGGER.info("Removed {} barrier blocks for dimension '{}' at radius {}", blocksRemoved, config.getName(), radius);
            return blocksRemoved;
        } else {
            LOGGER.warn("Could not find dimension '{}' to remove barriers", config.getName());
            return 0;
        }
    }
    
    /**
     * Removes barrier blocks from the specified radius.
     * @return number of blocks removed
     */
    private static int removeBarrierWall(ServerLevel level, int radius) {
        int blocksRemoved = 0;
        
        for (int y = MIN_BARRIER_Y; y <= MAX_BARRIER_Y; y++) {
            // Remove North and South walls
            for (int z = -radius; z <= radius; z++) {
                BlockPos northPos = new BlockPos(-radius, y, z);
                if (level.getBlockState(northPos).is(Blocks.BARRIER)) {
                    level.setBlock(northPos, Blocks.AIR.defaultBlockState(), 3);
                    blocksRemoved++;
                }
                
                BlockPos southPos = new BlockPos(radius, y, z);
                if (level.getBlockState(southPos).is(Blocks.BARRIER)) {
                    level.setBlock(southPos, Blocks.AIR.defaultBlockState(), 3);
                    blocksRemoved++;
                }
            }
            
            // Remove East and West walls
            for (int x = -radius + 1; x < radius; x++) {
                BlockPos westPos = new BlockPos(x, y, -radius);
                if (level.getBlockState(westPos).is(Blocks.BARRIER)) {
                    level.setBlock(westPos, Blocks.AIR.defaultBlockState(), 3);
                    blocksRemoved++;
                }
                
                BlockPos eastPos = new BlockPos(x, y, radius);
                if (level.getBlockState(eastPos).is(Blocks.BARRIER)) {
                    level.setBlock(eastPos, Blocks.AIR.defaultBlockState(), 3);
                    blocksRemoved++;
                }
            }
        }
        
        LOGGER.debug("Removed {} barrier blocks for radius {}", blocksRemoved, radius);
        return blocksRemoved;
    }
    
    /**
     * Refreshes barriers for a dimension (removes old ones and places new ones).
     * @return number of blocks placed
     */
    public static int refreshBarriers(MinecraftServer server, DimensionConfig config) {
        int removed = removeBarriers(server, config);
        int placed = placeBarriers(server, config);
        LOGGER.info("Refreshed barriers for dimension '{}': removed {}, placed {}", config.getName(), removed, placed);
        return placed;
    }
}
