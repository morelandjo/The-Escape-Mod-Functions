package com.theescapemod.functions.registry;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import com.theescapemod.functions.dimension.TEMFDimensions;

/**
 * Registers all datapack-based registries using the bootstrap system.
 * This follows the NeoForge 1.21.1 data generation pattern.
 */
public class TEMFDatapackRegistries {
    
    /**
     * Create a registry set builder with our bootstrap methods.
     * This is used during data generation to register worldgen data.
     */
    public static RegistrySetBuilder createBuilder() {
        return new RegistrySetBuilder()
                .add(Registries.DIMENSION_TYPE, TEMFDimensions::bootstrapDimensionTypes)
                .add(Registries.LEVEL_STEM, TEMFDimensions::bootstrapLevelStems);
    }
}
