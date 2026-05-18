package dev.merosssany.cinematica.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.merosssany.cinematica.core.data.death.DeathScreenSettings;
import dev.merosssany.cinematica.core.data.scrollingtext.CreditsSettings;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import dev.merosssany.cinematica.core.registry.settings.CreditScreenRegistry;
import dev.merosssany.cinematica.core.registry.settings.DeathScreenRegistry;
import dev.merosssany.cinematica.core.registry.settings.SlideshowRegistry;
import dev.merosssany.cinematica.core.security.ObjectKey;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSlide;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings.getGson;

public final class Cinematica {
    public static final String modId = "cinematica";
    
    private static final Map<SlideshowSettings, Path> slideshowRoots = new ConcurrentHashMap<>();
    private static final Logger logger = LogUtils.getLogger();
    private static ObjectKey lock;
    private static boolean frozen;
    
    private static SlideshowRegistry slideshowRegistry;
    private static DeathScreenRegistry deathScreenRegistry;
    private static CreditScreenRegistry creditScreenRegistry;
    
    public static void init(ObjectKey lock) {
        if (lock == null) {
            throw new IllegalArgumentException("ObjectKey cannot be null");
        }
        if (Cinematica.lock != null) {
            throw new IllegalStateException("Cinematica is already initialized!");
        }
        
        Cinematica.lock = lock;
        slideshowRegistry = new SlideshowRegistry(lock);
        deathScreenRegistry = new DeathScreenRegistry(lock);
        creditScreenRegistry = new CreditScreenRegistry(lock);
    }
    
    public static InputStream getAsset(String path, Path root) throws IOException {
        boolean isResource = path.matches("[a-z0-9_-]+:[a-z0-9_-]+");
        
        if (isResource) {
            ResourceLocation location = ResourceLocation.parse(path);
            Optional<Resource> optional = Minecraft.getInstance().getResourceManager().getResource(location);
            
            if (optional.isPresent()) {
                Resource resource = optional.get();
                return resource.open();
            }
            
            throw new FileNotFoundException("Couldn't find ResourceLocation: "+ location);
            
        } else {
            return new FileInputStream(root.resolve(path).toFile());
        }
    }
    
    public static void register(SlideshowSettings settings, Path root) throws InvalidJsonException {
        if (frozen) {
            throw new IllegalStateException("Cinematica is frozen!");
        }
        
        // Verifying the assets
        try {
            getAsset(settings.musicPath(), root).close();
        } catch (IOException e) {
            logger.error("Couldn't load music file", e);
        }
        
        SlideshowSlide[] slides = settings.slides();
        if (slides == null) throw new InvalidJsonException("There is no slides to register");
        
        for (SlideshowSlide slide : slides) {
            try {
                getAsset(slide.assetPath(), root).close();
            } catch (IOException e) {
                logger.error("Couldn't access the asset: {}", slide.assetPath());
            }
        }
        
        slideshowRegistry.register(settings);
        slideshowRoots.put(settings, root);
        logger.info("Registered slideshow \"{}\" successfully", settings.name());
    }
    
    public static SlideshowSettings getSlideshow(String name) {
        return slideshowRegistry.get(name);
    }
    
    public static void beginReload(ObjectKey key) {
        if (key != lock) throw new SecurityException();
        frozen = false;
        clear(key);
    }
    
    public static void clear(ObjectKey key) {
        slideshowRegistry.clear(key);
        creditScreenRegistry.clear(key);
        deathScreenRegistry.clear(key);
    }
    
    public static void freeze(ObjectKey key, boolean frozen) {
        slideshowRegistry.setFrozen(frozen, key);
        creditScreenRegistry.setFrozen(frozen, key);
        deathScreenRegistry.setFrozen(frozen, key);
    }
    
    public static boolean SlideshowExists(String name) {
        return slideshowRegistry.isRegistered(name);
    }
    
    public static LoadDetail[] reloadAll(ObjectKey key) throws IOException {
        beginReload(key);
        
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
        loadDeathScreens(root, folders);
        
        return root.toFile().getName();
    }
    
    private static void loadDeathScreens(Path root, JsonObject folders) throws IOException {
        if (folders.has("death_screen")) {
            String deathScreen = folders.get("death_screen").getAsString();
            File folder = root.resolve(deathScreen).toFile();
            
            if (folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files == null) return;
                
                for (File file : files) {
                    JsonObject j;
                    try (FileReader reader = new FileReader(file)) {
                        if (!file.getName().endsWith(".json")) continue;
                        j = JsonParser.parseReader(reader).getAsJsonObject();
                    }
                    
                    if (j.has("name")) {
                        String name = j.get("name").getAsString();
                        
                        if (slideshowRegistry.isRegistered(name)) {
                            deathScreenRegistry.register(getGson().fromJson(j, DeathScreenSettings.class));
                        }
                    }
                }
            }
        }
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
                    creditScreenRegistry.register(CreditsSettings.fromJson(j));
                } else logger.warn("Invalid credit screen: {}", file.getName());
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
                
                register(SlideshowSettings.fromJson(j), root);
            }
        }
    }
    
    public static Logger getLogger() {
        return logger;
    }
    
    public static Set<String> getSlideshows() {
        return slideshowRegistry.getRegistered();
    }
    
    public static String formalize(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9_\\-\\s]", "").replace(" ","_");
    }
    
    public static Path getRoot(SlideshowSettings settings) {
        return slideshowRoots.get(settings);
    }
    
    public static DeathScreenRegistry deathScreenRegistry() {
        return deathScreenRegistry;
    }
    
    public static CreditScreenRegistry creditScreenRegistry() {
        return creditScreenRegistry;
    }
    
    public record LoadDetail(Path path, String msg, boolean failure) {
    }
}
