package com.theescapemod.functions.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import com.theescapemod.functions.network.ScreenDisplayClientCache;
import com.theescapemod.functions.network.ScreenDisplaySyncPacket;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * Handles block interactions for the screen display system.
 * Checks for NBT tag "screendisp" and opens appropriate screens.
 */
public class ScreenDisplayHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String NBT_TAG = "screendisp";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        LOGGER.info("=== RIGHT CLICK EVENT TRIGGERED ===");
        
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        InteractionHand hand = event.getHand();
        
        LOGGER.info("Level: {}, Position: {}, Hand: {}, IsClientSide: {}", 
                   level, pos, hand, level.isClientSide);
        
        // Only handle on client side and with main hand for screen opening
        // But cancel on both sides to prevent chest opening
        if (hand != InteractionHand.MAIN_HAND) {
            LOGGER.info("Skipping - wrong hand");
            return;
        }
        
        // Check for screen display on both client and server
        String screenKey = null;
        BlockEntity blockEntity = null;
        
        if (level.isClientSide) {
            // On client, check cache first
            screenKey = ScreenDisplayClientCache.getScreenDisplay(pos);
            LOGGER.debug("Client cache lookup for {}: {}", pos, screenKey);
        }
        
        // If not in cache (or on server), check both block entity NBT and item frames
        if (screenKey == null) {
            // First check for item frames at this position
            AABB searchBox = new AABB(pos).inflate(0.5);
            var itemFrames = level.getEntitiesOfClass(ItemFrame.class, searchBox);
            
            for (ItemFrame itemFrame : itemFrames) {
                // Check if item frame has screen display NBT
                CompoundTag itemFrameNBT = new CompoundTag();
                itemFrame.saveWithoutId(itemFrameNBT);
                LOGGER.debug("Found item frame at {}: {}", pos, itemFrame);
                LOGGER.debug("Item frame NBT: {}", itemFrameNBT);
                
                if (itemFrameNBT.contains(NBT_TAG)) {
                    screenKey = itemFrameNBT.getString(NBT_TAG);
                    LOGGER.debug("Found screendisp in item frame with value: {}", screenKey);
                    break;
                }
            }
            
            // If no item frame found, check block entity (original functionality)
            if (screenKey == null) {
                blockEntity = level.getBlockEntity(pos);
                LOGGER.debug("Block entity at {}: {}", pos, blockEntity);
                
                if (blockEntity != null) {
                    // First try persistent data (this is more reliable for custom data)
                    String persistentScreenKey = blockEntity.getPersistentData().getString("screendisp");
                    if (!persistentScreenKey.isEmpty()) {
                        screenKey = persistentScreenKey;
                        LOGGER.debug("Found screendisp in persistent data with value: {}", screenKey);
                    } else {
                        // Try to get NBT data with full metadata (includes custom tags)
                        CompoundTag nbt = blockEntity.saveWithFullMetadata(level.registryAccess());
                        LOGGER.debug("Block entity NBT (full): {}", nbt);
                        
                        if (nbt.contains(NBT_TAG)) {
                            screenKey = nbt.getString(NBT_TAG);
                            LOGGER.debug("Found screendisp tag with value: {}", screenKey);
                            
                            // IMPORTANT: If we found it in NBT but not in persistent data,
                            // restore it to persistent data (this happens after world reload)
                            LOGGER.info("Restoring screendisp to persistent data after world reload");
                            blockEntity.getPersistentData().putString("screendisp", screenKey);
                            blockEntity.setChanged(); // Mark as dirty to ensure it saves
                        } else {
                            // Also try without metadata (the previous method)
                            CompoundTag nbtWithoutMeta = blockEntity.saveWithoutMetadata(level.registryAccess());
                            LOGGER.debug("Block entity NBT (without meta): {}", nbtWithoutMeta);
                            
                            if (nbtWithoutMeta.contains(NBT_TAG)) {
                                screenKey = nbtWithoutMeta.getString(NBT_TAG);
                                LOGGER.debug("Found screendisp tag in without-meta NBT with value: {}", screenKey);
                                
                                // Restore to persistent data
                                LOGGER.info("Restoring screendisp to persistent data from without-meta NBT");
                                blockEntity.getPersistentData().putString("screendisp", screenKey);
                                blockEntity.setChanged();
                            }
                        }
                    }
                }
            }
        }
        
        // On server side, if we don't find the NBT but the client cache had it, we should still cancel
        // This is a fallback to ensure both sides cancel even if NBT sync is problematic
        if (!level.isClientSide && screenKey == null && blockEntity != null) {
            // For now, we'll trust that if this is a chest and there's potential for screen display,
            // we should check if there might be a sync issue
            if (blockEntity instanceof net.minecraft.world.level.block.entity.ChestBlockEntity) {
                // We can't directly access client cache from server, but we can add server-side storage
                // For now, let's just log this scenario
                LOGGER.debug("Server side: Chest found but no screen key in NBT - this may indicate sync issues");
            }
        }
        
        // Special case: if client side has no data but this could be a block with item frame or chest
        // we should give the server a chance to cancel and send sync data
        if (level.isClientSide && screenKey == null) {
            // Check for item frames or chest block entities at this position
            AABB searchBox = new AABB(pos).inflate(0.5);
            var itemFrames = level.getEntitiesOfClass(ItemFrame.class, searchBox);
            boolean hasItemFrame = !itemFrames.isEmpty();
            boolean hasChestEntity = blockEntity instanceof net.minecraft.world.level.block.entity.ChestBlockEntity;
            
            if (hasItemFrame || hasChestEntity) {
                LOGGER.debug("Client side: Found item frame ({}) or chest entity ({}) - scheduling delayed check", hasItemFrame, hasChestEntity);
                
                // Schedule a delayed check in case the server sends sync data
                final BlockPos finalPos = pos.immutable();
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft != null) {
                    // Check after more ticks to ensure sync packet has time to arrive
                    minecraft.execute(() -> {
                        minecraft.execute(() -> {
                            minecraft.execute(() -> {
                                minecraft.execute(() -> {
                                    minecraft.execute(() -> {
                                        String delayedScreenKey = ScreenDisplayClientCache.getScreenDisplay(finalPos);
                                        LOGGER.debug("Delayed check for screen key at {}: {}", finalPos, delayedScreenKey);
                                        
                                        if (delayedScreenKey != null && !delayedScreenKey.isEmpty()) {
                                            LOGGER.info("Found screen key after sync: {}", delayedScreenKey);
                                            ScreenConfig.ScreenData screenData = ScreenLoader.getScreenConfig().getScreen(delayedScreenKey);
                                            
                                            if (screenData != null) {
                                                LOGGER.info("Opening delayed screen display for key: {}", delayedScreenKey);
                                                ScreenDisplayScreen screen = new ScreenDisplayScreen(screenData);
                                                minecraft.setScreen(screen);
                                            } else {
                                                LOGGER.warn("No screen configuration found for delayed key: {}", delayedScreenKey);
                                            }
                                        } else {
                                            LOGGER.debug("No screen key found after delayed check - interaction should proceed normally");
                                        }
                                    });
                                });
                            });
                        });
                    });
                }
            }
        }
        
        // If we found a screen key, cancel the event on both sides
        if (screenKey != null && !screenKey.isEmpty()) {
            LOGGER.debug("Block at {} has screendisp tag with value: {} (Side: {})", pos, screenKey, level.isClientSide ? "CLIENT" : "SERVER");
            
            // Cancel the event with SUCCESS to prevent the chest from opening
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            LOGGER.info("EVENT CANCELED WITH SUCCESS on {} - chest should not open!", level.isClientSide ? "CLIENT" : "SERVER");
            
            // On server side, ensure client has the data by sending sync packet
            if (!level.isClientSide) {
                LOGGER.info("Server: Sending sync packet to ensure client has screen data and open screen");
                PacketDistributor.sendToAllPlayers(new ScreenDisplaySyncPacket(pos, screenKey, true));
                
                // Also ensure it's in the client cache on server side for future reference
                // (This helps with consistency checking)
            }
            
            // Only open screen on client side
            if (level.isClientSide) {
                final String finalScreenKey = screenKey;
                
                // Get screen configuration
                ScreenConfig.ScreenData screenData = ScreenLoader.getScreenConfig().getScreen(finalScreenKey);
                
                if (screenData != null) {
                    LOGGER.info("Opening screen display for key: {}", finalScreenKey);
                    
                    // Open the screen on the client with a slight delay to ensure proper handling
                    Minecraft minecraft = Minecraft.getInstance();
                    if (minecraft != null) {
                        // Use a small delay to ensure the event is fully processed
                        minecraft.execute(() -> {
                            try {
                                LOGGER.info("Creating ScreenDisplayScreen with data: {}", screenData);
                                ScreenDisplayScreen screen = new ScreenDisplayScreen(screenData);
                                LOGGER.info("Setting screen...");
                                minecraft.setScreen(screen);
                                LOGGER.info("Screen set successfully!");
                            } catch (Exception e) {
                                LOGGER.error("Error opening screen display: ", e);
                            }
                        });
                    }
                } else {
                    LOGGER.warn("No screen configuration found for key: {}", finalScreenKey);
                    
                    // If no config found on client, wait a bit for the sync packet and try again
                    Minecraft minecraft = Minecraft.getInstance();
                    if (minecraft != null) {
                        minecraft.execute(() -> {
                            // Wait 1 tick and try again in case sync packet arrives
                            minecraft.execute(() -> {
                                String cachedKey = ScreenDisplayClientCache.getScreenDisplay(pos);
                                LOGGER.info("Retry after sync - cached key: {}", cachedKey);
                                if (cachedKey != null) {
                                    ScreenConfig.ScreenData retryScreenData = ScreenLoader.getScreenConfig().getScreen(cachedKey);
                                    if (retryScreenData != null) {
                                        LOGGER.info("Retry: Opening screen display for key: {}", cachedKey);
                                        ScreenDisplayScreen screen = new ScreenDisplayScreen(retryScreenData);
                                        minecraft.setScreen(screen);
                                    }
                                }
                            });
                        });
                    }
                }
            }
            
            // Return early to prevent any other processing
            return;
        }
    }
}
