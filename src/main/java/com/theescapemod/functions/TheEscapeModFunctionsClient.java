package com.theescapemod.functions;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TheEscapeModFunctions.MODID, value = Dist.CLIENT)
public class TheEscapeModFunctionsClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Client-specific setup
    }
}
