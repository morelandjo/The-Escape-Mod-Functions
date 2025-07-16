package com.theescapemod.functions.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * Handles placing a SimpleSchematic into a world at a specified location.
 */
public class SchematicPaster {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Paste a schematic into a world at the specified position.
     */
    public static boolean pasteSchematic(ServerLevel level, SimpleSchematic schematic, BlockPos targetPos, boolean replaceExisting, boolean includeBlockEntities) {
        LOGGER.info("=== PASTING SCHEMATIC ===");
        LOGGER.info("Target position: {}", targetPos);
        LOGGER.info("Dimension: {}", level.dimension().location());
        LOGGER.info("Replace existing: {}", replaceExisting);
        LOGGER.info("Include block entities: {}", includeBlockEntities);
        LOGGER.info("Schematic size: {}x{}x{}", schematic.width, schematic.height, schematic.length);
        LOGGER.info("Total blocks to place: {}", schematic.getAllBlocks().size());
        
        int blocksPlaced = 0;
        int blockEntitiesPlaced = 0;
        int blocksSkipped = 0;
        int blocksFailed = 0;
        
        try {
            // Place blocks
            for (Map.Entry<BlockPos, BlockState> entry : schematic.getAllBlocks().entrySet()) {
                BlockPos relativePos = entry.getKey();
                BlockState blockState = entry.getValue();
                
                // Calculate world position
                BlockPos worldPos = targetPos.offset(relativePos);
                
                // Check if we should replace existing blocks
                if (!replaceExisting && !level.isEmptyBlock(worldPos)) {
                    blocksSkipped++;
                    continue;
                }
                
                // Place the block
                boolean success = level.setBlock(worldPos, blockState, 3); // 3 = UPDATE_ALL
                if (success) {
                    blocksPlaced++;
                    if (blocksPlaced <= 5) { // Log first few blocks for debugging
                        LOGGER.debug("Placed block {} at {} (relative: {})", blockState, worldPos, relativePos);
                    }
                } else {
                    blocksFailed++;
                    if (blocksFailed <= 5) { // Log first few failures
                        LOGGER.warn("Failed to place block {} at {}", blockState, worldPos);
                    }
                }
            }
            
            // Place block entities if requested
            if (includeBlockEntities) {
                LOGGER.info("Placing {} block entities...", schematic.getBlockEntities().size());
                for (CompoundTag blockEntityTag : schematic.getBlockEntities()) {
                    if (placeBlockEntity(level, blockEntityTag, targetPos)) {
                        blockEntitiesPlaced++;
                    }
                }
            }
            
            LOGGER.info("=== PASTE COMPLETE ===");
            LOGGER.info("Blocks placed: {}", blocksPlaced);
            LOGGER.info("Blocks skipped: {}", blocksSkipped);
            LOGGER.info("Blocks failed: {}", blocksFailed);
            LOGGER.info("Block entities placed: {}", blockEntitiesPlaced);
            
            return blocksPlaced > 0 || blockEntitiesPlaced > 0;
            
        } catch (Exception e) {
            LOGGER.error("Error pasting schematic: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Place a single block entity from NBT data.
     */
    private static boolean placeBlockEntity(ServerLevel level, CompoundTag blockEntityTag, BlockPos basePos) {
        try {
            // Get position from NBT
            BlockPos relativePos;
            
            if (blockEntityTag.contains("Pos")) {
                // Sponge format - array of [x, y, z]
                int[] pos = blockEntityTag.getIntArray("Pos");
                if (pos.length >= 3) {
                    relativePos = new BlockPos(pos[0], pos[1], pos[2]);
                } else {
                    LOGGER.warn("Invalid Pos array in block entity: length {}", pos.length);
                    return false;
                }
            } else if (blockEntityTag.contains("x") && blockEntityTag.contains("y") && blockEntityTag.contains("z")) {
                // MCEdit format - individual x, y, z tags
                int x = blockEntityTag.getInt("x");
                int y = blockEntityTag.getInt("y");
                int z = blockEntityTag.getInt("z");
                relativePos = new BlockPos(x, y, z);
            } else {
                LOGGER.warn("Block entity missing position data");
                return false;
            }
            
            // Calculate world position
            BlockPos worldPos = basePos.offset(relativePos);
            
            // Get the block at this position
            BlockState blockState = level.getBlockState(worldPos);
            if (blockState.hasBlockEntity()) {
                // Create a new NBT tag with the correct world position
                CompoundTag worldBlockEntityTag = blockEntityTag.copy();
                worldBlockEntityTag.putInt("x", worldPos.getX());
                worldBlockEntityTag.putInt("y", worldPos.getY());
                worldBlockEntityTag.putInt("z", worldPos.getZ());
                
                // Remove the schematic position data
                worldBlockEntityTag.remove("Pos");
                
                // Get the existing block entity and load the data
                BlockEntity blockEntity = level.getBlockEntity(worldPos);
                if (blockEntity != null) {
                    blockEntity.loadWithComponents(worldBlockEntityTag, level.registryAccess());
                    blockEntity.setChanged();
                    return true;
                } else {
                    LOGGER.warn("No block entity found at {} for state {}", worldPos, blockState);
                }
            } else {
                LOGGER.debug("Block at {} does not support block entities", worldPos);
            }
            
            return false;
        } catch (Exception e) {
            LOGGER.error("Failed to place block entity: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Calculate the bounding box that would be affected by pasting this schematic.
     */
    public static BlockPos[] getBoundingBox(SimpleSchematic schematic, BlockPos targetPos) {
        BlockPos min = targetPos.offset(schematic.offset);
        BlockPos max = min.offset(schematic.width - 1, schematic.height - 1, schematic.length - 1);
        return new BlockPos[]{min, max};
    }
}
