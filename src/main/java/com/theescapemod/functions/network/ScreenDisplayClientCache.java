package com.theescapemod.functions.network;

import net.minecraft.core.BlockPos;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Client-side cache for screen display data.
 * Stores which blocks have screen display NBT tags since this data
 * doesn't automatically sync from server to client.
 */
public class ScreenDisplayClientCache {
    private static final Map<BlockPos, String> screenDisplays = new ConcurrentHashMap<>();
    
    /**
     * Set screen display data for a block position.
     */
    public static void setScreenDisplay(BlockPos pos, String screenKey) {
        if (screenKey == null || screenKey.isEmpty()) {
            screenDisplays.remove(pos);
        } else {
            screenDisplays.put(pos.immutable(), screenKey);
        }
    }
    
    /**
     * Get screen display data for a block position.
     * @param pos The block position
     * @return The screen key, or null if no screen display is set
     */
    public static String getScreenDisplay(BlockPos pos) {
        return screenDisplays.get(pos);
    }
    
    /**
     * Remove screen display data for a block position.
     */
    public static void removeScreenDisplay(BlockPos pos) {
        screenDisplays.remove(pos);
    }
    
    /**
     * Clear all cached screen display data.
     */
    public static void clear() {
        screenDisplays.clear();
    }
}
