package com.theescapemod.functions.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Handles network packet registration for The Escape Mod Functions.
 */
public class ModNetworking {
    
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        
        registrar.playToClient(
            ToastPacket.TYPE,
            ToastPacket.STREAM_CODEC,
            ToastPacket::handle
        );
        
        registrar.playToClient(
            ScreenDisplaySyncPacket.TYPE,
            ScreenDisplaySyncPacket.STREAM_CODEC,
            ScreenDisplaySyncPacket::handle
        );
    }
}
