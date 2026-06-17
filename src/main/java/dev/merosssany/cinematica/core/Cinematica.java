package dev.merosssany.cinematica.core;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import dev.merosssany.cinematica.core.data.RGBA;
import dev.merosssany.cinematica.core.data.handler.*;
import dev.merosssany.cinematica.core.data.loader.CinematicaProjectLoader;
import dev.merosssany.cinematica.core.data.loader.assets.*;
import dev.merosssany.cinematica.core.security.ObjectKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public final class Cinematica {
    public static final String MODID = "cinematica";
    private static final Logger logger = LogUtils.getLogger();
    public static boolean debug;
    private static ObjectKey key;
    
    public static void init(ObjectKey key) {
        if (Cinematica.key != null) return;
        Cinematica.key = key;
        SlideshowLoader slideshow = new SlideshowLoader(key);
        DeathScreenLoader deathScreen = new DeathScreenLoader(key);
        CreditsLoader credits = new CreditsLoader(key);
        DialogLoader dialog = new DialogLoader(key);
        CutsceneLoader cutscene = new CutsceneLoader(key);
        
        CinematicaProjectLoader.register("slideshows", slideshow);
        CinematicaProjectLoader.register("death_screens", deathScreen);
        CinematicaProjectLoader.register("credits", credits);
        CinematicaProjectLoader.register("dialogs", dialog);
        CinematicaProjectLoader.register("cutscene", cutscene);
    }
    
    public static InputStream getRawAudioStream(String soundId) throws Exception {
        ResourceLocation location = ResourceLocation.parse(soundId);
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.get(location);
        if (event == null) throw new Exception("SoundEvent not found: " + soundId);
        
        WeighedSoundEvents weighedEvents = Minecraft.getInstance().getSoundManager().getSoundEvent(location);
        if (weighedEvents == null) throw new Exception("No sound events defined for: " + soundId);
        
        Sound selectedSound = weighedEvents.getSound(RandomSource.create());
        ResourceLocation fileLoc = ResourceLocation.fromNamespaceAndPath(
                selectedSound.getLocation().getNamespace(),
                "sounds/" + selectedSound.getLocation().getPath() + ".ogg"
        );
        
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(fileLoc);
        if (resource.isPresent()) return resource.get().open();
        
        throw new Exception("File not found at: " + fileLoc);
    }
    
    public static InputStream getAsset(String path, Path root) throws IOException {
        // A path is a resource if root is null OR if it explicitly contains a colon character ':'
        boolean isResource = root == null || path.contains(":");
        
        if (isResource) {
            
            // Safe conversion step: handle cases where a colon may be missing for raw pack items
            ResourceLocation location = path.contains(":") ? ResourceLocation.parse(path) : ResourceLocation.fromNamespaceAndPath(MODID, path);
            Optional<Resource> optional = Minecraft.getInstance().getResourceManager().getResource(location);
            
            if (optional.isPresent()) {
                return optional.get().open();
            } else {
                try {
                    return getRawAudioStream(path);
                } catch (Exception e) {
                    throw new IOException("Couldn't find ResourceLocation inside pack resources: " + location, e);
                }
            }
            
        } else {
            return new FileInputStream(root.resolve(path).toFile());
        }
    }
    
    public static @NotNull GsonBuilder getGsonBuilder() {
        return new GsonBuilder()
                .registerTypeAdapter(Vector2i.class, new Vector2iAdapter())
                .registerTypeAdapter(Vector2f.class, new Vector2fAdapter())
                .registerTypeAdapter(Vector3d.class, new Vector3dAdapter())
                .registerTypeAdapter(RGBA.class, new RgbaAdaptor())
                .registerTypeAdapter(File.class, new FileAdapter())
                .registerTypeAdapter(Vector3i.class, new Vector3iAdapter())
                .setPrettyPrinting();
    }
    
    public static Logger getLogger() {
        return logger;
    }
    
    public static String formalize(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9_\\-\\s]", "").replace(" ","_");
    }
    
    public static void reloadAll(ObjectKey key) throws IOException {
        if (Cinematica.key == key) CinematicaProjectLoader.reload();
        else throw new SecurityException("Incorrect Key.");
    }
}
