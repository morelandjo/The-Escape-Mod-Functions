package com.theescapemod.functions.item;

import com.theescapemod.functions.TheEscapeModFunctions;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for all mod items.
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, TheEscapeModFunctions.MODID);

    public static final Supplier<Item> ASTRAL_COMMUNICATOR = ITEMS.register("astral_communicator",
            () -> new AstralCommunicatorItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
