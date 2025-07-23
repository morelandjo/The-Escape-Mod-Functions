package com.theescapemod.functions.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.theescapemod.functions.TheEscapeModFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI screen for displaying block-triggered screens.
 * Shows large text displays that are 75% of screen size and centered.
 */
public class ScreenDisplayScreen extends Screen {
    private static final float SCREEN_SCALE = 0.75f; // Screen is 75% of window size
    private static final int TYPEWRITER_SPEED = 1; // Ticks between characters (faster)
    private static final int CONTINUE_ARROW_SIZE = 20;
    
    // Texture for the screen background
    private static final ResourceLocation SCREEN_TEXTURE = ResourceLocation.fromNamespaceAndPath(
        TheEscapeModFunctions.MODID, "textures/gui/screen_display_background.png"
    );
    
    // Calculated screen dimensions and position
    private int screenWidth;
    private int screenHeight;
    private int screenX;
    private int screenY;
    private int contentMargin = 20;
    
    private ScreenConfig.ScreenData screenData;
    private String fullText = "";
    private String displayedText = "";
    private int textTimer = 0;
    private boolean textComplete = false;
    private boolean textSpeedUp = false;
    
    // Pagination for long text
    private List<String> textPages = new ArrayList<>();
    private int currentPage = 0;
    private boolean needsPagination = false;
    private boolean showContinueArrow = false;
    
    public ScreenDisplayScreen(ScreenConfig.ScreenData screenData) {
        super(Component.literal(screenData.title != null ? screenData.title : "Screen Display"));
        this.screenData = screenData;
        this.fullText = screenData.text != null ? screenData.text : "";
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render semi-transparent black background over the entire screen
        guiGraphics.fill(0, 0, width, height, 0x80000000);
        
        // Try to render the texture background for the screen area
        try {
            guiGraphics.blit(SCREEN_TEXTURE, screenX, screenY, 0, 0, screenWidth, screenHeight, screenWidth, screenHeight);
        } catch (Exception e) {
            // Fallback to a solid color background if texture is missing
            // Dark blue/gray background with border
            guiGraphics.fill(screenX, screenY, screenX + screenWidth, screenY + screenHeight, 0xD0203040);
            
            // Draw a simple border
            int borderColor = 0xFF4080C0;
            // Top border
            guiGraphics.fill(screenX, screenY, screenX + screenWidth, screenY + 4, borderColor);
            // Bottom border  
            guiGraphics.fill(screenX, screenY + screenHeight - 4, screenX + screenWidth, screenY + screenHeight, borderColor);
            // Left border
            guiGraphics.fill(screenX, screenY, screenX + 4, screenY + screenHeight, borderColor);
            // Right border
            guiGraphics.fill(screenX + screenWidth - 4, screenY, screenX + screenWidth, screenY + screenHeight, borderColor);
        }
    }

    @Override
    protected void init() {
        super.init();
        
        // Calculate screen dimensions (75% of window size, centered)
        screenWidth = (int) (width * SCREEN_SCALE);
        screenHeight = (int) (height * SCREEN_SCALE);
        screenX = (width - screenWidth) / 2;
        screenY = (height - screenHeight) / 2;
        
        setupTextPagination();
        startTextDisplay();
    }
    
    /**
     * Setup text pagination based on available screen space
     */
    private void setupTextPagination() {
        textPages.clear();
        currentPage = 0;
        
        if (fullText.isEmpty()) {
            textPages.add("");
            return;
        }
        
        // Calculate available text area within the screen
        int availableWidth = screenWidth - (contentMargin * 2);
        int availableHeight = screenHeight - (contentMargin * 2) - 40; // Reserve space for continue arrow
        int lineHeight = font.lineHeight + 2;
        int maxLines = availableHeight / lineHeight;
        
        // Split text into words
        String[] words = fullText.split(" ");
        List<String> currentPageLines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            int lineWidth = font.width(testLine);
            
            if (lineWidth > availableWidth && currentLine.length() > 0) {
                // Line would be too long, finish current line and start new one
                currentPageLines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
                
                // Check if we've filled the page
                if (currentPageLines.size() >= maxLines) {
                    // Start new page
                    textPages.add(String.join("\n", currentPageLines));
                    currentPageLines.clear();
                }
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }
        
        // Add any remaining text
        if (currentLine.length() > 0) {
            currentPageLines.add(currentLine.toString());
        }
        if (!currentPageLines.isEmpty()) {
            textPages.add(String.join("\n", currentPageLines));
        }
        
        needsPagination = textPages.size() > 1;
        
        if (textPages.isEmpty()) {
            textPages.add("");
        }
    }
    
    /**
     * Start displaying text for the current page
     */
    private void startTextDisplay() {
        if (currentPage < textPages.size()) {
            fullText = textPages.get(currentPage);
            displayedText = "";
            textTimer = 0;
            textComplete = false;
            textSpeedUp = false;
            showContinueArrow = false;
        }
    }

    @Override
    public void tick() {
        super.tick();
        
        // Typewriter effect
        if (displayedText.length() < fullText.length()) {
            textTimer++;
            if (textSpeedUp || textTimer >= TYPEWRITER_SPEED) {
                if (textSpeedUp) {
                    // Show all text instantly
                    displayedText = fullText;
                } else {
                    textTimer = 0;
                    displayedText = fullText.substring(0, displayedText.length() + 1);
                }
            }
        } else {
            if (!textComplete) {
                textComplete = true;
                // Show continue arrow if there are more pages
                if (needsPagination && currentPage < textPages.size() - 1) {
                    showContinueArrow = true;
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render background
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        // Calculate text area within the screen
        int textX = screenX + contentMargin;
        int textY = screenY + contentMargin;
        int textWidth = screenWidth - (contentMargin * 2);
        
        // Draw title if present
        if (screenData.title != null && !screenData.title.isEmpty()) {
            int titleWidth = font.width(screenData.title);
            int titleX = screenData.center_text ? screenX + (screenWidth - titleWidth) / 2 : textX;
            guiGraphics.drawString(font, screenData.title, titleX, textY, screenData.text_color);
            textY += font.lineHeight + 10; // Add spacing after title
        }
        
        // Draw main text
        if (!displayedText.isEmpty()) {
            if (screenData.center_text) {
                drawCenteredWrappedText(guiGraphics, displayedText, textX, textY, textWidth, screenData.text_color);
            } else {
                drawWrappedText(guiGraphics, displayedText, textX, textY, textWidth, screenData.text_color);
            }
        }
        
        // Draw continue arrow if needed
        if (showContinueArrow) {
            drawContinueArrow(guiGraphics, screenX + screenWidth - contentMargin - CONTINUE_ARROW_SIZE, 
                            screenY + screenHeight - contentMargin - CONTINUE_ARROW_SIZE);
        }
        
        // Draw instructions at the bottom of the window (not in the screen area)
        if (textComplete && !showContinueArrow) {
            String instruction = "Press SPACE to close or ESC to close";
            int instructionWidth = font.width(instruction);
            guiGraphics.drawString(font, instruction, (width - instructionWidth) / 2, height - 20, 0xAAAAAA);
        } else if (showContinueArrow) {
            String instruction = "Press SPACE or click to continue, ESC to close";
            int instructionWidth = font.width(instruction);
            guiGraphics.drawString(font, instruction, (width - instructionWidth) / 2, height - 20, 0xAAAAAA);
        }
        
        // Render buttons and other widgets on top
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    /**
     * Draw text with word wrapping (left-aligned)
     */
    private void drawWrappedText(GuiGraphics guiGraphics, String text, int x, int y, int maxWidth, int color) {
        if (text == null || text.isEmpty()) return;
        
        String[] lines = text.split("\n");
        int currentY = y;
        int lineHeight = font.lineHeight + 2;
        
        for (String line : lines) {
            String[] words = line.split(" ");
            int currentX = x;
            
            for (String word : words) {
                int wordWidth = font.width(word + " ");
                if (currentX + wordWidth > x + maxWidth && currentX > x) {
                    currentX = x;
                    currentY += lineHeight;
                }
                guiGraphics.drawString(font, word, currentX, currentY, color);
                currentX += wordWidth;
            }
            currentY += lineHeight; // Move to next line after each \n
        }
    }
    
    /**
     * Draw text with word wrapping (centered)
     */
    private void drawCenteredWrappedText(GuiGraphics guiGraphics, String text, int x, int y, int maxWidth, int color) {
        if (text == null || text.isEmpty()) return;
        
        String[] lines = text.split("\n");
        int currentY = y;
        int lineHeight = font.lineHeight + 2;
        
        for (String line : lines) {
            // Build lines that fit within maxWidth
            String[] words = line.split(" ");
            StringBuilder currentLine = new StringBuilder();
            List<String> wrappedLines = new ArrayList<>();
            
            for (String word : words) {
                String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
                if (font.width(testLine) > maxWidth && currentLine.length() > 0) {
                    wrappedLines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    currentLine = new StringBuilder(testLine);
                }
            }
            if (currentLine.length() > 0) {
                wrappedLines.add(currentLine.toString());
            }
            
            // Draw each wrapped line centered
            for (String wrappedLine : wrappedLines) {
                int lineWidth = font.width(wrappedLine);
                int centeredX = x + (maxWidth - lineWidth) / 2;
                guiGraphics.drawString(font, wrappedLine, centeredX, currentY, color);
                currentY += lineHeight;
            }
        }
    }
    
    /**
     * Draw a white arrow indicating more content
     */
    private void drawContinueArrow(GuiGraphics guiGraphics, int x, int y) {
        // Draw a simple triangle pointing right
        int color = 0xFFFFFFFF; // White
        
        // Draw triangle by filling individual pixels (simple approach)
        for (int i = 0; i < CONTINUE_ARROW_SIZE; i++) {
            int width = i < CONTINUE_ARROW_SIZE / 2 ? i : CONTINUE_ARROW_SIZE - i;
            int startY = y + CONTINUE_ARROW_SIZE / 2 - width / 2;
            for (int j = 0; j <= width; j++) {
                guiGraphics.fill(x + i, startY + j, x + i + 1, startY + j + 1, color);
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC key
            onClose();
            return true;
        } else if ((keyCode == 32 || keyCode == 257)) { // Space or Enter
            if (displayedText.length() < fullText.length()) {
                // Speed up typewriter effect
                textSpeedUp = true;
                return true;
            } else if (showContinueArrow) {
                // Move to next page
                nextPage();
                return true;
            } else {
                // Close screen
                onClose();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Left click
        if (button == 0) {
            if (displayedText.length() < fullText.length()) {
                // Speed up typewriter effect
                textSpeedUp = true;
                return true;
            } else if (showContinueArrow) {
                // Move to next page
                nextPage();
                return true;
            } else {
                // Close screen
                onClose();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    /**
     * Move to the next page of text
     */
    private void nextPage() {
        if (currentPage < textPages.size() - 1) {
            currentPage++;
            startTextDisplay();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
