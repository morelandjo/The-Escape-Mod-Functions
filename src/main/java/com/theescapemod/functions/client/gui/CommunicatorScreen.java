package com.theescapemod.functions.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.theescapemod.functions.communication.CommunicationConfig;
import com.theescapemod.functions.communication.SceneManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI screen for the Astral Communicator.
 * Displays messages in RPG style with typewriter effect.
 */
public class CommunicatorScreen extends Screen {
    private static final int TEXT_BOX_HEIGHT = 80; // Made shorter
    private static final int TEXT_BOX_MAX_WIDTH = 300; // Standard Minecraft GUI width
    private static final int NAME_BOX_WIDTH = 120;
    private static final int NAME_BOX_HEIGHT = 25;
    private static final int IMAGE_BOX_SIZE = 32; // Square image box
    private static final int IMAGE_BOX_PADDING = 5; // Padding between image box and name box
    private static final int TYPEWRITER_SPEED = 2; // Ticks between characters
    
    private CommunicationConfig.Scene currentScene;
    private int currentMessageIndex = 0;
    private String displayedText = "";
    private String fullText = "";
    private int textTimer = 0;
    private List<Button> optionButtons = new ArrayList<>();
    private int selectedOption = -1;
    private boolean messageComplete = false;
    private boolean waitingForInput = false;
    private boolean textSpeedUp = false; // Track if text should speed up
    
    // Store state to prevent restart on resize
    private int savedMessageIndex = 0;
    private String savedDisplayedText = "";
    private int savedSelectedOption = -1;
    
    // Transition effect for speaker changes
    private boolean inTransition = false;
    private int transitionTimer = 0;
    private static final int TRANSITION_DURATION = 5; // Ticks to hide UI during speaker change
    private String lastSpeakerName = "";

    public CommunicatorScreen(CommunicationConfig.Scene scene) {
        super(Component.literal("Astral Communicator"));
        this.currentScene = scene;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Create a more transparent dark background for better visibility
        guiGraphics.fill(0, 0, width, height, 0x30000000);
    }

    @Override
    protected void init() {
        super.init();
        // Don't restart on resize - use saved state if available
        if (savedMessageIndex > 0) {
            currentMessageIndex = savedMessageIndex;
            displayedText = savedDisplayedText;
            selectedOption = savedSelectedOption;
            startMessage(currentMessageIndex, true); // true = preserve progress
        } else {
            startMessage(0, false);
        }
    }

    /**
     * Start displaying a message at the given index.
     */
    private void startMessage(int index, boolean preserveProgress) {
        if (index >= currentScene.messages.size()) {
            // Scene complete
            if (minecraft != null && minecraft.player != null) {
                SceneManager.markSceneComplete(minecraft.player.getUUID(), currentScene.scene_number);
            }
            onClose();
            return;
        }
        
        // Save state for resize protection
        savedMessageIndex = index;
        currentMessageIndex = index;
        CommunicationConfig.Message msg = currentScene.messages.get(index);
        
        // Check for speaker change to trigger transition effect
        String currentSpeakerName = msg.name != null ? msg.name : "";
        String processedCurrentSpeakerName = replacePlaceholders(currentSpeakerName);
        String processedLastSpeakerName = replacePlaceholders(lastSpeakerName);
        
        if (!preserveProgress && !processedLastSpeakerName.isEmpty() && !processedLastSpeakerName.equals(processedCurrentSpeakerName)) {
            // Speaker changed, start transition effect
            inTransition = true;
            transitionTimer = 0;
        }
        lastSpeakerName = currentSpeakerName;
        
        clearOptionButtons();
        
        // Set the text to display
        String rawText = "";
        if ("answer".equals(msg.type) && selectedOption >= 0 && msg.answers != null) {
            rawText = msg.answers.get(Math.min(selectedOption, msg.answers.size() - 1));
        } else if ("reply".equals(msg.type) && selectedOption >= 0 && msg.replies != null) {
            rawText = msg.replies.get(Math.min(selectedOption, msg.replies.size() - 1));
        } else {
            rawText = msg.message != null ? msg.message : "";
        }
        
        // Replace %player% with actual player name
        fullText = replacePlaceholders(rawText);
        
        if (!preserveProgress) {
            displayedText = "";
            textTimer = 0;
            savedDisplayedText = "";
        } else {
            savedDisplayedText = displayedText;
        }
        
        textSpeedUp = false;
        messageComplete = false;
        waitingForInput = false;
        
        // Create option buttons for questions
        if ("question".equals(msg.type) && msg.options != null) {
            // Wait for text to complete before showing options
            waitingForInput = true;
        }
    }

    /**
     * Create option buttons for question messages.
     */
    private void createOptionButtons() {
        CommunicationConfig.Message msg = currentScene.messages.get(currentMessageIndex);
        if (!"question".equals(msg.type) || msg.options == null) return;
        
        clearOptionButtons();
        
        int startY = height - TEXT_BOX_HEIGHT - 80;
        for (int i = 0; i < msg.options.size(); i++) {
            final int optionIndex = i;
            String optionText = replacePlaceholders(msg.options.get(i));
            Button button = Button.builder(
                Component.literal((i + 1) + ". " + optionText),
                b -> selectOption(optionIndex)
            ).bounds(width - 320, startY - (i * 25), 300, 20).build();
            
            addRenderableWidget(button);
            optionButtons.add(button);
        }
    }

    /**
     * Handle option selection for question messages.
     */
    private void selectOption(int option) {
        selectedOption = option;
        savedSelectedOption = option;
        clearOptionButtons();
        nextMessage();
    }

    /**
     * Clear all option buttons.
     */
    private void clearOptionButtons() {
        optionButtons.forEach(this::removeWidget);
        optionButtons.clear();
    }

    /**
     * Move to the next message in the scene.
     */
    private void nextMessage() {
        startMessage(currentMessageIndex + 1, false);
    }

    /**
     * Replace placeholders in text with actual values.
     */
    private String replacePlaceholders(String text) {
        if (text == null) return "";
        
        String result = text;
        
        // Replace %player% with actual player name
        if (minecraft != null && minecraft.player != null) {
            result = result.replace("%player%", minecraft.player.getName().getString());
        }
        
        return result;
    }

    @Override
    public void tick() {
        super.tick();
        
        // Handle transition effect for speaker changes
        if (inTransition) {
            transitionTimer++;
            if (transitionTimer >= TRANSITION_DURATION) {
                inTransition = false;
                transitionTimer = 0;
            }
            // During transition, don't update typewriter effect
            return;
        }
        
        // Typewriter effect
        if (displayedText.length() < fullText.length()) {
            textTimer++;
            if (textSpeedUp || textTimer >= TYPEWRITER_SPEED) {
                if (textSpeedUp) {
                    // Show all text instantly
                    displayedText = fullText;
                    savedDisplayedText = displayedText;
                } else {
                    textTimer = 0;
                    displayedText = fullText.substring(0, displayedText.length() + 1);
                    savedDisplayedText = displayedText;
                }
            }
        } else {
            if (!messageComplete) {
                messageComplete = true;
                // Show option buttons if this is a question
                if (waitingForInput) {
                    createOptionButtons();
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Use Minecraft's proper background rendering instead of manual overlay
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        // During transition, only show the transparent background
        if (inTransition) {
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }
        
        // Calculate centered text box with max width
        int textBoxWidth = Math.min(TEXT_BOX_MAX_WIDTH, width - 40);
        int textBoxX = (width - textBoxWidth) / 2;
        int textBoxY = height - TEXT_BOX_HEIGHT - 20;
        
        // Draw text box
        guiGraphics.fill(textBoxX, textBoxY, textBoxX + textBoxWidth, height - 20, 0xE0000000);
        guiGraphics.fill(textBoxX + 2, textBoxY + 2, textBoxX + textBoxWidth - 2, height - 22, 0xFF1a1a1a);
        
        // Draw image and name box
        if (currentMessageIndex < currentScene.messages.size()) {
            CommunicationConfig.Message currentMsg = currentScene.messages.get(currentMessageIndex);
            if (currentMsg.name != null) {
                // Determine positioning based on name_side
                boolean isRightSide = "right".equals(currentMsg.name_side);
                int nameBoxX = isRightSide ? 
                    textBoxX + textBoxWidth - NAME_BOX_WIDTH - 10 : // Right side
                    textBoxX + 10; // Left side (default)
                int nameBoxY = textBoxY - NAME_BOX_HEIGHT;
                
                // Draw image box above name box
                if (currentMsg.image != null) {
                    int imageBoxX = nameBoxX + (NAME_BOX_WIDTH - IMAGE_BOX_SIZE) / 2; // Center above name box
                    int imageBoxY = nameBoxY - IMAGE_BOX_SIZE - IMAGE_BOX_PADDING;
                    
                    // Draw image box background
                    guiGraphics.fill(imageBoxX, imageBoxY, imageBoxX + IMAGE_BOX_SIZE, imageBoxY + IMAGE_BOX_SIZE, 0xFF333333);
                    guiGraphics.fill(imageBoxX + 1, imageBoxY + 1, imageBoxX + IMAGE_BOX_SIZE - 1, imageBoxY + IMAGE_BOX_SIZE - 1, 0xFF1a1a1a);
                    
                    // Draw the image/player model
                    drawSpeakerImage(guiGraphics, currentMsg.image, imageBoxX + 2, imageBoxY + 2, IMAGE_BOX_SIZE - 4);
                }
                
                // Draw name box
                guiGraphics.fill(nameBoxX, nameBoxY, nameBoxX + NAME_BOX_WIDTH, textBoxY - 5, 0xFF333333);
                guiGraphics.fill(nameBoxX + 2, nameBoxY + 2, nameBoxX + NAME_BOX_WIDTH - 2, textBoxY - 7, 0xFF555555);
                
                // Draw name with placeholder replacement
                String displayName = replacePlaceholders(currentMsg.name);
                guiGraphics.drawString(font, displayName, nameBoxX + 8, nameBoxY + 8, 0xFFFFFF);
            }
            
            // Draw message text with word wrapping
            drawWrappedText(guiGraphics, displayedText, textBoxX + 10, textBoxY + 10, textBoxWidth - 20, 0xFFFFFF);
        }
        
        // Draw instruction text
        if (messageComplete && optionButtons.isEmpty() && !waitingForInput) {
            String instruction = "Press SPACE to continue or ESC to close";
            int instructionWidth = font.width(instruction);
            guiGraphics.drawString(font, instruction, (width - instructionWidth) / 2, height - 35, 0xAAAAA);
        }
        
        // Render buttons and other widgets on top
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * Draw text with word wrapping.
     */
    private void drawWrappedText(GuiGraphics guiGraphics, String text, int x, int y, int maxWidth, int color) {
        if (text == null || text.isEmpty()) return;
        
        String[] words = text.split(" ");
        int currentX = x;
        int currentY = y;
        int lineHeight = font.lineHeight + 2;
        
        for (String word : words) {
            int wordWidth = font.width(word + " ");
            if (currentX + wordWidth > x + maxWidth && currentX > x) {
                currentX = x;
                currentY += lineHeight;
            }
            guiGraphics.drawString(font, word, currentX, currentY, color);
            currentX += wordWidth;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC key
            if (minecraft != null && minecraft.player != null) {
                SceneManager.clearPendingScene(minecraft.player.getUUID());
            }
            onClose();
            return true;
        } else if ((keyCode == 32 || keyCode == 257) && messageComplete && optionButtons.isEmpty() && !waitingForInput) { // Space or Enter
            nextMessage();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Left click speeds up text or advances message
        if (button == 0) {
            if (displayedText.length() < fullText.length()) {
                // Speed up typewriter effect
                textSpeedUp = true;
                return true;
            } else if (messageComplete && optionButtons.isEmpty() && !waitingForInput) {
                // Advance to next message
                nextMessage();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    /**
     * Draw speaker image or player model in the image box.
     */
    private void drawSpeakerImage(GuiGraphics guiGraphics, String imageId, int x, int y, int size) {
        if ("%player%".equals(imageId)) {
            // Draw player model
            drawPlayerModel(guiGraphics, x, y, size);
        } else {
            // Draw texture from comms folder
            drawTextureImage(guiGraphics, imageId, x, y, size);
        }
    }
    
    /**
     * Draw a player head in the image box.
     */
    private void drawPlayerModel(GuiGraphics guiGraphics, int x, int y, int size) {
        if (minecraft != null && minecraft.player != null) {
            try {
                // Get the player's skin texture from their player renderer
                var playerRenderer = minecraft.getEntityRenderDispatcher().getRenderer(minecraft.player);
                if (playerRenderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer playerRend) {
                    ResourceLocation skinTexture = playerRend.getTextureLocation(minecraft.player);
                    
                    // Calculate head position to center it in the box
                    int headSize = (int)(size * 0.8f); // Use 80% of box size for the head
                    int headX = x + (size - headSize) / 2;
                    int headY = y + (size - headSize) / 2;
                    
                    // Bind the player's skin texture
                    RenderSystem.setShaderTexture(0, skinTexture);
                    
                    // Draw the front face of the head (UV coordinates for head front face)
                    // The head texture is 8x8 pixels starting at (8, 8) in a 64x64 skin texture
                    guiGraphics.blit(skinTexture, headX, headY, headSize, headSize, 8.0f, 8.0f, 8, 8, 64, 64);
                    
                    // Draw the hat/overlay layer if present (UV coordinates for hat front face)
                    // The hat texture is 8x8 pixels starting at (40, 8) in a 64x64 skin texture
                    RenderSystem.enableBlend();
                    guiGraphics.blit(skinTexture, headX, headY, headSize, headSize, 40.0f, 8.0f, 8, 8, 64, 64);
                    RenderSystem.disableBlend();
                } else {
                    // If we can't get the renderer, draw a fallback
                    drawFallbackPlayerImage(guiGraphics, x, y, size);
                }
                
            } catch (Exception e) {
                // If skin rendering fails, draw a fallback
                drawFallbackPlayerImage(guiGraphics, x, y, size);
            }
        } else {
            // If minecraft or player is null, draw a fallback
            drawFallbackPlayerImage(guiGraphics, x, y, size);
        }
    }
    
    /**
     * Draw a fallback image when player model rendering fails.
     */
    private void drawFallbackPlayerImage(GuiGraphics guiGraphics, int x, int y, int size) {
        // Draw a simple player head silhouette as fallback
        guiGraphics.fill(x + size/4, y + size/4, x + 3*size/4, y + 3*size/4, 0xFF8B4513); // Brown for head
        guiGraphics.fill(x + size/3, y + size/3, x + 2*size/3, y + size/2, 0xFFFFDDDD); // Light skin tone
    }
    
    /**
     * Draw a texture from the comms folder.
     */
    private void drawTextureImage(GuiGraphics guiGraphics, String textureId, int x, int y, int size) {
        try {
            ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(
                "theescapemodfunctions", "textures/comms/" + textureId + ".png");
            
            // Bind and draw the texture
            RenderSystem.setShaderTexture(0, textureLocation);
            guiGraphics.blit(textureLocation, x, y, 0, 0, size, size, size, size);
        } catch (Exception e) {
            // If texture loading fails, draw a placeholder
            drawPlaceholderImage(guiGraphics, textureId, x, y, size);
        }
    }
    
    /**
     * Draw a placeholder when texture loading fails.
     */
    private void drawPlaceholderImage(GuiGraphics guiGraphics, String textureId, int x, int y, int size) {
        // Draw a colored square as placeholder
        guiGraphics.fill(x, y, x + size, y + size, 0xFF666666);
        
        // Draw the first letter of the texture ID in the center
        if (textureId != null && !textureId.isEmpty()) {
            String letter = textureId.substring(0, 1).toUpperCase();
            int letterWidth = font.width(letter);
            int letterX = x + (size - letterWidth) / 2;
            int letterY = y + (size - font.lineHeight) / 2;
            guiGraphics.drawString(font, letter, letterX, letterY, 0xFFFFFF);
        }
    }
}
