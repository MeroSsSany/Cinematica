package dev.merosssany.cinematica;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.FileManager;
import dev.merosssany.cinematica.core.audio.data.factory.DefaultFactories;
import dev.merosssany.cinematica.core.data.CinematicaProjectLoader;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import dev.merosssany.cinematica.core.security.ObjectKey;
import dev.merosssany.cinematica.registry.ModAttachments;
import dev.merosssany.cinematica.registry.command.CinematicaCommands;
import dev.merosssany.cinematica.registry.command.ModArgumentTypes;
import dev.merosssany.cinematica.renderer.slideshow.SlideshowScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Mod(Cinematica.MODID)
public class NeoForgeCinematica {
    private static SlideshowSettings settings;
    private static final ObjectKey key = new ObjectKey();
    
    // Key-binding parameters
    public static final String CATEGORY = "key.categories.cinematica";
    public static final KeyMapping OPEN_INTRO = new KeyMapping(
            "key.cinematica.open_intro",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            CATEGORY
    );
    
    public NeoForgeCinematica(IEventBus modEventBus) {
        Cinematica.init(key);
        DefaultFactories.register();
        
        // Register Mod Bus variables
        ModArgumentTypes.register(modEventBus);
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::onClientSetup);
        ModAttachments.register(modEventBus);
        
        CinematicaProjectLoader.key(key);
        NeoForge.EVENT_BUS.register(this);
        
        try {
            FileManager.init();
            Cinematica.reloadAll(key);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Cinematica filesystem configurations", e);
        }
    }
    
    public JsonObject readJsonFile(File file) throws Exception {
        try (FileReader reader = new FileReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
    
    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent e) {
        CinematicaCommands.register(e.getDispatcher(), key);
    }
    
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
    
    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_INTRO);
    }
    
    private void onClientSetup(FMLClientSetupEvent event) {
    }
    
    @EventBusSubscriber(modid = Cinematica.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class ClientTickHandler {
        
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            while (OPEN_INTRO.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (settings != null) {
                    mc.setScreen(new SlideshowScreen(settings));
                }
            }
        }
    }
}