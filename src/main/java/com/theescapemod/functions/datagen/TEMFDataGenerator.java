package com.theescapemod.functions.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import com.theescapemod.functions.TheEscapeModFunctions;
import com.theescapemod.functions.registry.TEMFDatapackRegistries;
import java.util.concurrent.CompletableFuture;

/**
 * Data generator for TEMF worldgen data.
 * This generates the dimension and dimension type registries at build time.
 */
public class TEMFDataGenerator {
    
    /**
     * Register data providers for data generation event.
     */
    public static void register(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        
        // Add the datapack builtin entries provider
        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(
                output, 
                lookupProvider, 
                TEMFDatapackRegistries.createBuilder(), 
                java.util.Set.of(TheEscapeModFunctions.MODID)
        ));
    }
}
