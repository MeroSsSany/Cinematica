package dev.merosssany.cinematica.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.merosssany.cinematica.core.data.scrollingtext.CreditsSettings;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import dev.merosssany.cinematica.ObjectKey;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSlide;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Cinematica {
    public static final String MODID = "cinematica";
    
    private static final Map<String, SlideshowSettings> slideshows = new ConcurrentHashMap<>();
    private static final Map<String, CreditsSettings> credits = new ConcurrentHashMap<>();
    private static final Logger logger = LogUtils.getLogger();
    
    private static ObjectKey lock;
    private static boolean frozen;
    
    public static void init(ObjectKey lock) {
        if (lock == null) {
            throw new IllegalArgumentException("ObjectKey cannot be null");
        }
        if (Cinematica.lock != null) {
            throw new IllegalStateException("Cinematica is already initialized!");
        }
        
        Cinematica.lock = lock;
    }
    
    public static void register(SlideshowSettings settings) {
        if (frozen) {
            throw new IllegalStateException("Cinematica is frozen!");
        }
        
        // Verifying the assets
        if (settings.musicPath().exists()) {
            logger.error("Music file does not exist: {}", settings.musicPath().getAbsolutePath());
        }
        
        for (SlideshowSlide slide : settings.slides()) {
            if (slide.assetPath().exists()) continue;
            logger.error("Asset does not exist: {}", slide.assetPath().getAbsolutePath());
        }
        
        slideshows.put(settings.name(), settings);
        logger.info("Registered slideshow \"{}\" successfully", settings.name());
    }
    
    public static void register(CreditsSettings settings) {
        if (frozen) {
            throw new IllegalStateException("Cinematica is frozen!");
        }
        credits.put(settings.name(), settings);
        logger.info("Registered credits screen \"{}\" successfully", settings.name());
    }
    
    public static SlideshowSettings getSlideshow(String name) {
        return slideshows.get(name);
    }
    
    public static CreditsSettings getCredits(String name) {
        return credits.get(name);
    }
    
    public static void freeze(ObjectKey key) {
        if (key == lock) frozen = true;
    }
    
    public static void beginReload(ObjectKey key) {
        if (key != lock) throw new SecurityException();
        frozen = false;
        clear(key);
    }
    
    public static void clear(ObjectKey key) {
        if (frozen) {
            throw new IllegalStateException("Cannot clear while frozen");
        }
        if (key == lock) {
            slideshows.clear();
            credits.clear();
        }
    }
    
    public static boolean SlideshowExists(String name) {
        return slideshows.containsKey(name);
    }
    
    public static boolean CreditsExists(String name) {
        return credits.containsKey(name);
    }
    
    public static LoadDetail[] reloadAll(ObjectKey key) throws IOException {
        if (lock != key) {
            throw new SecurityException("Invalid ObjectKey");
        }
        clear(key);
        
        Path cinematica = FileManager.getCinematicaFolder();
        
        if (Files.isDirectory(cinematica)) {
            File[] files = new File(cinematica.toUri()).listFiles();
            
            if (files == null) return null;
            
            LoadDetail[] details = new LoadDetail[files.length];
            
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                
                if (file.isDirectory()) {
                    try {
                        details[i] = new LoadDetail(
                                file.toPath(),
                                load(file.toPath()) + " has been loaded successfully.",
                                false
                        );
                        
                    } catch (Exception e) {
                        Cinematica.logger.error("Failed to load cinematic at {}", file.toPath(), e);
                        details[i] = new LoadDetail(file.toPath(), e.getMessage(), true);
                    }
                }
            }
            
            return details;
        }
        return null;
    }
    
    public static String load(Path root) throws IOException, InvalidJsonException {
        if (frozen) throw new IllegalStateException("Cinematica is frozen!");
        
        Path metadata = root.resolve("cinematica.json");
        
        JsonObject metadataJson;
        try (FileReader reader = new FileReader(metadata.toFile())) {
            metadataJson = JsonParser.parseReader(reader).getAsJsonObject();
        }
        
        if (!metadataJson.has("version"))
            throw new InvalidJsonException("Couldn't find property \"version\". This property is mandatory.");
        if (!metadataJson.has("folders"))
            throw new InvalidJsonException("Couldn't find property \"folders\". This property is mandatory.");
        
        JsonObject folders = metadataJson.get("folders").getAsJsonObject();
        
        loadSlideshows(root, folders);
        loadCreditScreens(root, folders);
        
        return root.toFile().getName();
    }
    
    private static void loadCreditScreens(Path root, JsonObject folders) throws InvalidJsonException, IOException {
        if (folders.has("scrolling_text")) {
            String scrollingText = folders.get("scrolling_text").getAsString();
            Path scrollingTextFolder = root.resolve(scrollingText);
            File scrollingTextFile = scrollingTextFolder.toFile();
            if (!scrollingTextFile.isDirectory())
                throw new InvalidJsonException("\"" + scrollingText + "\" must be a folder.");
            File[] files = scrollingTextFile.listFiles();
            if (files == null) return; // This shouldn't happen
            
            for (File file : files) {
                JsonObject j;
                try (FileReader reader = new FileReader(file)) {
                    if (!file.getName().endsWith(".json")) continue;
                    j = JsonParser.parseReader(reader).getAsJsonObject();
                }
                
                if (CreditsSettings.isValid(j)) {
                    register(CreditsSettings.fromJson(j));
                }
            }
        }
    }
    
    private static void loadSlideshows(Path root, JsonObject folders) throws InvalidJsonException, IOException {
        if (folders.has("slideshows")) {
            // Load the slideshow
            String slideshowsFolderName = folders.get("slideshows").getAsString();
            Path slideshows = root.resolve(slideshowsFolderName);
            File slideshowsFile = slideshows.toFile();
            
            if (!slideshowsFile.isDirectory())
                throw new InvalidJsonException("\"" + slideshowsFolderName + "\" must be a folder.");
            File[] slideshowSettings = slideshowsFile.listFiles();
            
            if (slideshowSettings == null) return;
            
            for (File file : slideshowSettings) {
                JsonObject j;
                
                try (FileReader reader = new FileReader(file)) {
                    if (!file.getName().endsWith(".json")) continue;
                    j = JsonParser.parseReader(reader).getAsJsonObject();
                }
                
                register(SlideshowSettings.fromJson(j, root));
            }
        }
    }
    
    public static Logger getLogger() {
        return logger;
    }
    
    public static Set<String> getSlideshows() {
        return slideshows.keySet();
    }
    
    public record LoadDetail(Path path, String msg, boolean failure) {
    }
}
