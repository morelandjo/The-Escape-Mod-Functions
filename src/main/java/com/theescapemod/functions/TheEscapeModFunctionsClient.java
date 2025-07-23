package com.theescapemod.functions;

import com.theescapemod.functions.item.AstralCommunicatorItem;
import com.theescapemod.functions.item.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TheEscapeModFunctions.MODID, value = Dist.CLIENT)
public class TheEscapeModFunctionsClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Client-specific setup
        event.enqueueWork(() -> {
            // Register item property for astral communicator active state
            ItemProperties.register(
                ModItems.ASTRAL_COMMUNICATOR.get(),
                ResourceLocation.fromNamespaceAndPath(TheEscapeModFunctions.MODID, "active"),
                AstralCommunicatorItem.createActivePropertyFunction()
            );
        });
    }
}
