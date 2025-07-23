package com.theescapemod.functions.screens;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration structure for the screen display system.
 * Defines text content for block-triggered screens.
 */
public class ScreenConfig {
    public Map<String, ScreenData> screens = new HashMap<>();

    public static class ScreenData {
        public String text;
        public String title;
        public int text_color = 0xFFFFFF; // Default white
        public int background_color = 0xE0000000; // Default semi-transparent black
        public boolean center_text = false; // Whether to center text or left-align
    }

    /**
     * Get screen data by its key.
     */
    public ScreenData getScreen(String key) {
        return screens.get(key);
    }

    /**
     * Check if a screen exists.
     */
    public boolean hasScreen(String key) {
        return screens.containsKey(key);
    }
}
