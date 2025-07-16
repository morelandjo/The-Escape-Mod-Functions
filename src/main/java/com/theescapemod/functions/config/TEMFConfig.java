package com.theescapemod.functions.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TEMFConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_DIMENSION_LOADING = BUILDER
            .comment("Enable automatic loading of dimension configurations from config/temf/dimensions/")
            .define("enableDimensionLoading", true);

    public static final ModConfigSpec.IntValue DEFAULT_WORLD_BORDER_SIZE = BUILDER
            .comment("Default world border size for dimensions when not specified")
            .defineInRange("defaultWorldBorderSize", 1000, 100, 100000);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
