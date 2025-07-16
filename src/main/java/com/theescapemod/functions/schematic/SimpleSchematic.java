package com.theescapemod.functions.schematic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * Simple schematic data structure that holds loaded schematic information.
 * This is a simplified version that doesn't require WorldEdit dependencies.
 */
public class SimpleSchematic {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public final int width;
    public final int height;
    public final int length;
    public final BlockPos offset;
    
    private final Map<BlockPos, BlockState> blocks = new HashMap<>();
    private final List<CompoundTag> blockEntities = new ArrayList<>();
    
    public SimpleSchematic(int width, int height, int length, BlockPos offset) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.offset = offset;
    }
    
    public void setBlock(BlockPos pos, BlockState state) {
        blocks.put(pos, state);
    }
    
    public BlockState getBlock(BlockPos pos) {
        return blocks.get(pos);
    }
    
    public void addBlockEntity(CompoundTag blockEntity) {
        blockEntities.add(blockEntity);
    }
    
    public Map<BlockPos, BlockState> getAllBlocks() {
        return blocks;
    }
    
    public List<CompoundTag> getBlockEntities() {
        return blockEntities;
    }
    
    /**
     * Converts a block state string (like "minecraft:stone[variant=granite]") to a BlockState.
     * This is a simplified version that handles basic block states.
     */
    public static BlockState parseBlockState(String blockStateString) {
        try {
            // Handle simple case: "minecraft:stone"
            if (!blockStateString.contains("[")) {
                ResourceLocation blockId = ResourceLocation.parse(blockStateString);
                Block block = BuiltInRegistries.BLOCK.get(blockId);
                return block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState();
            }
            
            // Handle properties: "minecraft:stone[variant=granite]"
            int bracketIndex = blockStateString.indexOf("[");
            String blockId = blockStateString.substring(0, bracketIndex);
            // For now, we'll just use the default state and ignore properties
            // A full implementation would parse the properties and apply them
            
            ResourceLocation blockResourceLocation = ResourceLocation.parse(blockId);
            Block block = BuiltInRegistries.BLOCK.get(blockResourceLocation);
            
            if (block != null) {
                // TODO: Parse and apply block properties from the bracket section
                return block.defaultBlockState();
            }
            
            return Blocks.AIR.defaultBlockState();
        } catch (Exception e) {
            LOGGER.warn("Failed to parse block state '{}': {}", blockStateString, e.getMessage());
            return Blocks.AIR.defaultBlockState();
        }
    }
    
    /**
     * Converts legacy block ID + data to modern BlockState.
     * This is used for MCEdit format schematics.
     */
    public static BlockState legacyToBlockState(int blockId, int data) {
        // This is a very simplified conversion
        // A full implementation would use proper legacy mapping tables
        try {
            if (blockId == 0) return Blocks.AIR.defaultBlockState();
            if (blockId == 1) return Blocks.STONE.defaultBlockState();
            if (blockId == 2) return Blocks.GRASS_BLOCK.defaultBlockState();
            if (blockId == 3) return Blocks.DIRT.defaultBlockState();
            if (blockId == 4) return Blocks.COBBLESTONE.defaultBlockState();
            if (blockId == 5) return Blocks.OAK_PLANKS.defaultBlockState();
            // Add more mappings as needed...
            
            LOGGER.warn("Unknown legacy block ID: {} with data: {}", blockId, data);
            return Blocks.STONE.defaultBlockState(); // Fallback
        } catch (Exception e) {
            LOGGER.warn("Failed to convert legacy block {}/{}: {}", blockId, data, e.getMessage());
            return Blocks.AIR.defaultBlockState();
        }
    }
}
