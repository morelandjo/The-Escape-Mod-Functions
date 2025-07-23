package com.theescapemod.functions.network;

import com.theescapemod.functions.TheEscapeModFunctions;
import com.theescapemod.functions.screens.ScreenConfig;
import com.theescapemod.functions.screens.ScreenLoader;
import com.theescapemod.functions.screens.ScreenDisplayScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet to sync screen display data from server to client.
 * This packet tells the client which blocks have screen display NBT tags.
 */
public record ScreenDisplaySyncPacket(BlockPos pos, String screenKey, boolean openScreen) implements CustomPacketPayload {
    
    public static final Type<ScreenDisplaySyncPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(TheEscapeModFunctions.MODID, "screen_display_sync")
    );
    
    public static final StreamCodec<RegistryFriendlyByteBuf, ScreenDisplaySyncPacket> STREAM_CODEC = 
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, ScreenDisplaySyncPacket::pos,
            StreamCodec.of((buf, value) -> buf.writeUtf(value), buf -> buf.readUtf()), ScreenDisplaySyncPacket::screenKey,
            StreamCodec.of((buf, value) -> buf.writeBoolean(value), buf -> buf.readBoolean()), ScreenDisplaySyncPacket::openScreen,
            ScreenDisplaySyncPacket::new
        );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(ScreenDisplaySyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Store the screen display data on the client side
            System.out.println("PACKET RECEIVED: Setting screen display at " + packet.pos() + " to " + packet.screenKey() + ", openScreen: " + packet.openScreen());
            ScreenDisplayClientCache.setScreenDisplay(packet.pos(), packet.screenKey());
            System.out.println("PACKET HANDLED: Cache now contains: " + ScreenDisplayClientCache.getScreenDisplay(packet.pos()));
            
            // If requested, open the screen immediately
            if (packet.openScreen()) {
                System.out.println("PACKET: Opening screen for key: " + packet.screenKey());
                
                ScreenConfig.ScreenData screenData = ScreenLoader.getScreenConfig().getScreen(packet.screenKey());
                
                if (screenData != null) {
                    System.out.println("PACKET: Creating and opening screen");
                    ScreenDisplayScreen screen = new ScreenDisplayScreen(screenData);
                    Minecraft minecraft = Minecraft.getInstance();
                    if (minecraft != null) {
                        minecraft.setScreen(screen);
                        System.out.println("PACKET: Screen opened successfully!");
                    }
                } else {
                    System.out.println("PACKET: No screen data found for key: " + packet.screenKey());
                }
            }
        });
    }
}
