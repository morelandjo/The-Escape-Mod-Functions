package com.theescapemod.functions;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.theescapemod.functions.commands.TEMFCommands;
import com.theescapemod.functions.config.TEMFConfig;
import com.theescapemod.functions.datagen.TEMFDataGenerator;
import com.theescapemod.functions.dimension.DimensionChecker;
import com.theescapemod.functions.dimension.DimensionManager;
import com.theescapemod.functions.dimension.DimensionConfig;
import com.theescapemod.functions.world.WorldBorderManager;
import com.theescapemod.functions.world.BarrierManager;
import com.theescapemod.functions.schematic.SchematicManager;
import com.theescapemod.functions.communication.CommunicationLoader;
import com.theescapemod.functions.screens.ScreenLoader;
import com.theescapemod.functions.screens.ScreenDisplayHandler;
import com.theescapemod.functions.item.ModItems;
import com.theescapemod.functions.item.ModCreativeTabs;
import com.theescapemod.functions.network.ModNetworking;

import java.util.Map;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TheEscapeModFunctions.MODID)
public class TheEscapeModFunctions {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "theescapemodfunctions";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public TheEscapeModFunctions(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        
        // Register data generation
        modEventBus.addListener(TEMFDataGenerator::register);
        
        // Register network packets
        modEventBus.addListener(ModNetworking::register);

        // Register items and creative tabs
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, TEMFConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("The Escape Mod Functions - Common Setup");
        
        // Initialize dimension manager
        DimensionManager.init();
        
        // Initialize schematic manager
        LOGGER.info("Initializing schematic manager...");
        SchematicManager.init();
        
        // Initialize schematic system
        SchematicManager.init();
        
        // Initialize communication system
        LOGGER.info("Initializing communication system...");
        CommunicationLoader.init();
        
        // Initialize screen display system
        LOGGER.info("Initializing screen display system...");
        ScreenLoader.init();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Server starting - dimensions should now be registered
        LOGGER.info("The Escape Mod Functions - Server Starting");
        
        // Load dimension configurations for world borders and other settings
        DimensionManager.loadDimensionConfigs();
        
        DimensionChecker.checkAllDimensions(event.getServer());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        // Apply world borders after server has fully started and dimensions are loaded
        LOGGER.info("The Escape Mod Functions - Server Started, applying world borders and barriers");
        Map<String, DimensionConfig> loadedDimensions = DimensionManager.getLoadedDimensions();
        WorldBorderManager.applyWorldBorders(event.getServer(), loadedDimensions);
        BarrierManager.placeAllBarriers(event.getServer(), loadedDimensions);
        
        // Execute schematic imports
        LOGGER.info("Starting schematic imports...");
        SchematicManager.executeImports(event.getServer());
        LOGGER.info("Finished schematic imports.");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TEMFCommands.register(event.getDispatcher());
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("The Escape Mod Functions - Client Setup");
            
            // Register screen display event handler
            NeoForge.EVENT_BUS.register(ScreenDisplayHandler.class);
        }
    }
}
