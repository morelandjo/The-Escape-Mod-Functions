package com.theescapemod.functions.network;

import com.theescapemod.functions.TheEscapeModFunctions;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Network packet to trigger toast notifications on the client.
 */
public record ToastPacket(String title, String description) implements CustomPacketPayload {
    
    public static final Type<ToastPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(TheEscapeModFunctions.MODID, "toast")
    );
    
    public static final StreamCodec<ByteBuf, ToastPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ToastPacket::title,
        ByteBufCodecs.STRING_UTF8, ToastPacket::description,
        ToastPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(ToastPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.getToasts() != null) {
                    SystemToast.add(
                        minecraft.getToasts(),
                        SystemToast.SystemToastId.NARRATOR_TOGGLE, // Using existing toast type
                        net.minecraft.network.chat.Component.literal(packet.title()),
                        net.minecraft.network.chat.Component.literal(packet.description())
                    );
                }
            }
        });
    }
}
