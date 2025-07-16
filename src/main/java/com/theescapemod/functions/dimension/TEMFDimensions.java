package com.theescapemod.functions.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import com.theescapemod.functions.TheEscapeModFunctions;
import java.util.OptionalLong;
import java.util.Optional;
import java.util.List;

/**
 * Handles code-based dimension registration using bootstrap methods.
 * This replaces the static datapack approach for proper NeoForge 1.21.1 compatibility.
 */
public class TEMFDimensions {
    
    // Void Spawn Dimension
    public static final ResourceLocation VOID_SPAWN_ID = ResourceLocation.fromNamespaceAndPath(TheEscapeModFunctions.MODID, "void_spawn");
    public static final ResourceKey<DimensionType> VOID_SPAWN_DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, VOID_SPAWN_ID);
    public static final ResourceKey<LevelStem> VOID_SPAWN_LEVEL_STEM = ResourceKey.create(Registries.LEVEL_STEM, VOID_SPAWN_ID);
    
    /**
     * Bootstrap dimension types - called during data generation
     */
    public static void bootstrapDimensionTypes(BootstrapContext<DimensionType> context) {
        // Register void_spawn dimension type
        context.register(VOID_SPAWN_DIMENSION_TYPE, new DimensionType(
                OptionalLong.empty(), // Fixed time - empty means normal day/night cycle
                false, // Has skylight - disable for dark sky like the End
                false, // Has ceiling (like nether)
                false, // Ultra warm (like nether)
                false, // Natural (prevents spawning in void)
                1.0,   // Coordinate scale
                false, // Bed works
                false, // Respawn anchor works
                -64,   // Min Y
                384,   // Height
                384,   // Logical height
                BlockTags.INFINIBURN_OVERWORLD, // Infiniburn blocks
                ResourceLocation.fromNamespaceAndPath("minecraft", "the_end"), // Use End effects for dark sky
                0.0f,  // Ambient light level
                new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0) // Monster settings - no spawning
        ));
    }
    
    /**
     * Bootstrap level stems (dimensions) - called during data generation
     */
    public static void bootstrapLevelStems(BootstrapContext<LevelStem> context) {
        HolderGetter<Biome> biomeGetter = context.lookup(Registries.BIOME);
        
        // Create flat settings for a completely empty world (void)
        FlatLevelGeneratorSettings voidSettings = new FlatLevelGeneratorSettings(
                Optional.empty(), // No structure overrides
                biomeGetter.getOrThrow(Biomes.THE_VOID), // Use void biome
                List.of() // Empty layers - no blocks at all
        );
        
        // Register void_spawn dimension with flat generator (empty = void)
        context.register(VOID_SPAWN_LEVEL_STEM, new LevelStem(
                context.lookup(Registries.DIMENSION_TYPE).getOrThrow(VOID_SPAWN_DIMENSION_TYPE),
                new FlatLevelSource(voidSettings)
        ));
    }
}
