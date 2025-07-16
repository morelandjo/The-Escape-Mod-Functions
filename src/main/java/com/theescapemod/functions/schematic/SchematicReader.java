package com.theescapemod.functions.schematic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import net.minecraft.nbt.NbtAccounter;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * Reads schematic files in various formats (Sponge v1/v2/v3, MCEdit)
 * and converts them to our simplified SimpleSchematic format.
 */
public class SchematicReader {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Read a schematic file and return a SimpleSchematic object.
     * Automatically detects the format based on the NBT structure.
     */
    public static SimpleSchematic readSchematic(File schematicFile) throws IOException {
        if (!schematicFile.exists()) {
            throw new IOException("Schematic file does not exist: " + schematicFile.getAbsolutePath());
        }
        
        LOGGER.info("=== READING SCHEMATIC ===");
        LOGGER.info("File: {}", schematicFile.getName());
        LOGGER.info("Size: {} bytes", schematicFile.length());
        LOGGER.info("Path: {}", schematicFile.getAbsolutePath());
        
        try (FileInputStream fis = new FileInputStream(schematicFile)) {
            LOGGER.info("Reading NBT data...");
            CompoundTag nbt = NbtIo.readCompressed(schematicFile.toPath(), NbtAccounter.unlimitedHeap());
            LOGGER.info("NBT data loaded successfully");
            LOGGER.info("Root NBT keys: {}", nbt.getAllKeys());
            
            // Detect format based on NBT structure
            // Check if we have a Sponge schematic (with nested Schematic compound)
            if (nbt.contains("Schematic")) {
                CompoundTag schematicTag = nbt.getCompound("Schematic");
                LOGGER.info("Found Schematic compound, keys: {}", schematicTag.getAllKeys());
                if (schematicTag.contains("Version")) {
                    int version = schematicTag.getInt("Version");
                    LOGGER.info("Detected Sponge Schematic v{}", version);
                    return readSpongeSchematic(schematicTag, version);
                }
            }
            // Check for direct Version field (older format)
            else if (nbt.contains("Version")) {
                int version = nbt.getInt("Version");
                LOGGER.info("Detected Sponge Schematic v{}", version);
                return readSpongeSchematic(nbt, version);
            } 
            // Check for MCEdit format
            else if (nbt.contains("Materials")) {
                String materials = nbt.getString("Materials");
                LOGGER.info("Detected materials field: {}", materials);
                if ("Alpha".equals(materials)) {
                    LOGGER.info("Detected MCEdit schematic");
                    return readMCEditSchematic(nbt);
                }
            }
            
            LOGGER.error("Unknown schematic format - NBT keys: {}", nbt.getAllKeys());
            throw new IOException("Unknown schematic format");
        } catch (Exception e) {
            LOGGER.error("Failed to read schematic: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Read a Sponge format schematic (v1, v2, or v3)
     */
    private static SimpleSchematic readSpongeSchematic(CompoundTag nbt, int version) throws IOException {
        int width = nbt.getShort("Width") & 0xFFFF;
        int height = nbt.getShort("Height") & 0xFFFF;
        int length = nbt.getShort("Length") & 0xFFFF;
        
        LOGGER.info("Schematic dimensions: {}x{}x{}", width, height, length);
        
        // Get offset
        BlockPos offset = BlockPos.ZERO;
        if (nbt.contains("Offset")) {
            int[] offsetArray = nbt.getIntArray("Offset");
            if (offsetArray.length >= 3) {
                offset = new BlockPos(offsetArray[0], offsetArray[1], offsetArray[2]);
            }
        }
        
        SimpleSchematic schematic = new SimpleSchematic(width, height, length, offset);
        
        if (version >= 3) {
            // Version 3 has blocks in a separate compound
            CompoundTag blocks = nbt.getCompound("Blocks");
            readSpongeBlocks(schematic, blocks, width, height, length);
        } else {
            // Version 1 and 2 have blocks at root level
            readSpongeBlocks(schematic, nbt, width, height, length);
        }
        
        // Read block entities
        String blockEntitiesKey = version >= 3 ? "BlockEntities" : "TileEntities";
        if (version >= 3 && nbt.getCompound("Blocks").contains("BlockEntities")) {
            ListTag blockEntities = nbt.getCompound("Blocks").getList("BlockEntities", 10);
            for (int i = 0; i < blockEntities.size(); i++) {
                schematic.addBlockEntity(blockEntities.getCompound(i));
            }
        } else if (nbt.contains(blockEntitiesKey)) {
            ListTag blockEntities = nbt.getList(blockEntitiesKey, 10);
            for (int i = 0; i < blockEntities.size(); i++) {
                schematic.addBlockEntity(blockEntities.getCompound(i));
            }
        }
        
        return schematic;
    }
    
    /**
     * Read blocks from Sponge format (uses palette + data)
     */
    private static void readSpongeBlocks(SimpleSchematic schematic, CompoundTag blocksTag, int width, int height, int length) {
        // Read palette
        CompoundTag paletteTag = blocksTag.getCompound("Palette");
        Map<Integer, BlockState> palette = new HashMap<>();
        
        for (String key : paletteTag.getAllKeys()) {
            int id = paletteTag.getInt(key);
            BlockState blockState = SimpleSchematic.parseBlockState(key);
            palette.put(id, blockState);
            
            if (palette.size() <= 10) { // Only log first few for debugging
                LOGGER.debug("Palette entry {}: {} -> {}", id, key, blockState);
            }
        }
        
        LOGGER.info("Loaded palette with {} entries", palette.size());
        
        // Read block data
        byte[] blockData = blocksTag.getByteArray("BlockData");
        if (blocksTag.contains("Data")) {
            blockData = blocksTag.getByteArray("Data");
        }
        
        // Decode VarInt encoded block data
        int dataIndex = 0;
        
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    if (dataIndex >= blockData.length) break;
                    
                    // VarInt decoding
                    int blockId = 0;
                    int position = 0;
                    byte currentByte;
                    
                    do {
                        if (dataIndex >= blockData.length) break;
                        currentByte = blockData[dataIndex++];
                        blockId |= (currentByte & 0x7F) << position;
                        position += 7;
                    } while ((currentByte & 0x80) != 0 && position < 32);
                    
                    BlockState blockState = palette.get(blockId);
                    if (blockState != null) {
                        schematic.setBlock(new BlockPos(x, y, z), blockState);
                    }
                }
            }
        }
        
        LOGGER.info("Loaded {} blocks from schematic", schematic.getAllBlocks().size());
    }
    
    /**
     * Read an MCEdit format schematic
     */
    private static SimpleSchematic readMCEditSchematic(CompoundTag nbt) throws IOException {
        short width = nbt.getShort("Width");
        short height = nbt.getShort("Height");
        short length = nbt.getShort("Length");
        
        LOGGER.info("MCEdit schematic dimensions: {}x{}x{}", width, height, length);
        
        SimpleSchematic schematic = new SimpleSchematic(width, height, length, BlockPos.ZERO);
        
        // Read blocks
        byte[] blocks = nbt.getByteArray("Blocks");
        byte[] data = nbt.getByteArray("Data");
        
        // Read additional block IDs if present (for block IDs > 255)
        byte[] addBlocks = new byte[0];
        if (nbt.contains("AddBlocks")) {
            addBlocks = nbt.getByteArray("AddBlocks");
        }
        
        // Convert blocks
        for (int index = 0; index < blocks.length; index++) {
            int x = index % width;
            int z = (index / width) % length;
            int y = index / (width * length);
            
            int blockId = blocks[index] & 0xFF;
            int blockData = data[index] & 0xFF;
            
            // Handle additional block IDs
            if (addBlocks.length > 0) {
                if ((index & 1) == 0) {
                    blockId |= (addBlocks[index >> 1] & 0x0F) << 8;
                } else {
                    blockId |= (addBlocks[index >> 1] & 0xF0) << 4;
                }
            }
            
            BlockState blockState = SimpleSchematic.legacyToBlockState(blockId, blockData);
            schematic.setBlock(new BlockPos(x, y, z), blockState);
        }
        
        // Read tile entities
        if (nbt.contains("TileEntities")) {
            ListTag tileEntities = nbt.getList("TileEntities", 10);
            for (int i = 0; i < tileEntities.size(); i++) {
                schematic.addBlockEntity(tileEntities.getCompound(i));
            }
        }
        
        LOGGER.info("Loaded {} blocks from MCEdit schematic", schematic.getAllBlocks().size());
        return schematic;
    }
}
