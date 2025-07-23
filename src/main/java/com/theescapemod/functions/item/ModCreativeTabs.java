package com.theescapemod.functions.item;

import com.theescapemod.functions.TheEscapeModFunctions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Creative tabs for the mod.
 */
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheEscapeModFunctions.MODID);

    public static final Supplier<CreativeModeTab> TEMF_TAB = CREATIVE_MODE_TABS.register("temf_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.ASTRAL_COMMUNICATOR.get()))
                    .title(Component.translatable("creativetab.theescapemodfunctions.temf_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ASTRAL_COMMUNICATOR.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
