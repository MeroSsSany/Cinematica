package dev.merosssany.cinematica;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.FileManager;
import dev.merosssany.cinematica.core.audio.data.factory.DefaultFactories;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import dev.merosssany.cinematica.registry.command.CinematicaCommands;
import dev.merosssany.cinematica.registry.command.ModArgumentTypes;
import dev.merosssany.cinematica.renderer.SlideshowScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

import java.io.*;

@Mod(Cinematica.MODID)
public class ForgeCinematica {
    private static SlideshowSettings settings;
    private static final ObjectKey key = new ObjectKey();
    
    public ForgeCinematica() {
        Cinematica.init(key);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        DefaultFactories.register();
        ModArgumentTypes.register();
        
        try {
            FileManager.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        try {
             String name = Cinematica.load(FileManager.getCinematicaFolder().resolve("test"));
             settings = Cinematica.getSlideshow(Cinematica.getSlideshows().iterator().next());
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public JsonObject readJsonFile(File file) throws Exception {
        try (FileReader reader = new FileReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
    
    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent e) {
        CinematicaCommands.register(e.getDispatcher());
    }
    
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
    
    public static final String CATEGORY = "key.categories.cinematica";
    
    // The "Open Intro" key - Defaulted to 'K'
    public static final KeyMapping OPEN_INTRO = new KeyMapping(
            "key.cinematica.open_intro", // Translation key
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,             // Default key
            CATEGORY                     // Category in Options menu
    );
    
    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(OPEN_INTRO);
    }
    
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Cinematica.MODID)
    public static class c {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                while (OPEN_INTRO.consumeClick()) {
                    Minecraft mc = Minecraft.getInstance();
                    
                    mc.setScreen(new SlideshowScreen(settings));
                }
            }
        }
    }
}
