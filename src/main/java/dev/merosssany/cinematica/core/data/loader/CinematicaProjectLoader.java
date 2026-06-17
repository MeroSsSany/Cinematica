package dev.merosssany.cinematica.core.data.loader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.FileManager;
import dev.merosssany.cinematica.core.data.CinematicaAsset;
import dev.merosssany.cinematica.core.data.loader.assets.SlideshowLoader;
import dev.merosssany.cinematica.core.registry.core.CinematicaAssetsRegistry;
import dev.merosssany.cinematica.core.security.ObjectKey;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

public class CinematicaProjectLoader {
    private static final ObjectKey key = new ObjectKey();
    private static final CinematicaAssetsRegistry registry = new CinematicaAssetsRegistry(key);
    private static final Logger logger = Cinematica.getLogger();
    
    public static void register(String name, CinematicaAssetLoader<?> cinematicaAssetLoader) {
        registry.register(name, cinematicaAssetLoader);
    }
    
    @SubscribeEvent
    public static void onLoadingComplete(FMLClientSetupEvent e) {
        registry.setFrozen(true, key);
    }
    
    public static CinematicaAssetsRegistry registry() {
        return registry;
    }
    
    public static void loadFolder(Path root) throws IOException {
        Path cinematica = root.resolve("cinematica.json");
        
        if (Files.exists(cinematica)) {
            JsonObject config;
            try (FileReader reader = new FileReader(cinematica.toFile())) {
                config = JsonParser.parseReader(reader).getAsJsonObject();
            }
            
            if (config.has("folders")) {
                JsonElement folders = config.get("folders");
                if (folders.isJsonArray()) {
                    
                    for (var assets : folders.getAsJsonArray()) {
                        if (assets.isJsonPrimitive()) {
                            String path = assets.getAsString();
                            if (!registry.isRegistered(path)) continue; // Safeguard if loader doesn't exist
                            
                            CinematicaAssetLoader<?> asset = registry.get(path);
                            Path resolvedPath = root.resolve(path);
                            
                            if (Files.isDirectory(resolvedPath)) {
                                try (var stream = Files.list(resolvedPath)) {
                                    for (Iterator<@NotNull Path> it = stream.iterator(); it.hasNext(); ) {
                                        Path file = it.next();
                                        if (!file.toString().endsWith(".json")) continue; // Don't try to parse non-json junk
                                        
                                        JsonObject object;
                                        try (Reader reader = new FileReader(file.toFile())) {
                                            object = JsonParser.parseReader(reader).getAsJsonObject();
                                        }
                                        
                                        processAndRegister(asset, object);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static <T extends CinematicaAsset> void processAndRegister(CinematicaAssetLoader<T> loader, JsonObject object) {
        Validation validation = loader.validate(object);
        if (validation.success()) {
            T resource = loader.createFromJson(object);
            loader.register(resource);
        } else {
            // Log validation error cleanly
            logger.error("Validation failed for asset: {}", validation.reason());
        }
    }
    
    public static void loadProjects(ResourceManager resourceManager) {
        Map<ResourceLocation, Resource> manifests = resourceManager.listResources("",
                loc -> loc.getPath().endsWith("cinematica.json")
        );
        
        for (Map.Entry<ResourceLocation, Resource> entry : manifests.entrySet()) {
            ResourceLocation manifestId = entry.getKey();
            Resource resource = entry.getValue();
            String namespace = manifestId.getNamespace();
            String fullPath = manifestId.getPath();
            String basePath = fullPath.substring(0, fullPath.length() - "cinematica.json".length());
            
            try (Reader reader = resource.openAsReader()) {
                JsonObject metadataJson = JsonParser.parseReader(reader).getAsJsonObject();
                
                if (!metadataJson.has("version") || !metadataJson.has("folders")) {
                    logger.error("Skipping invalid pack project manifest: {}", manifestId);
                    continue;
                }
                
                JsonElement foldersElement = metadataJson.get("folders");
                if (!foldersElement.isJsonObject()) {
                    logger.error("Manifest at {} has an invalid format: 'folders' must be a JSON Object schema.", manifestId);
                    continue;
                }
                
                JsonObject folders = foldersElement.getAsJsonObject();
                loadPack(resourceManager, namespace, folders, basePath);
                logger.info("Successfully loaded Cinematica pack project from namespace '{}' using base path '{}'", namespace, basePath);
                
            } catch (Exception e) {
                logger.error("Failed to process Cinematica project manifest at {}", manifestId, e);
            }
        }
    }
    
    private static void loadPack(ResourceManager resourceManager, String namespace, JsonObject folders, String basePath) {
        for (var folderSet : folders.entrySet()) {
            if (registry.isRegistered(folderSet.getKey())) {
                CinematicaAssetLoader<?> loader = registry.get(folderSet.getKey());
                String folderName = folderSet.getValue().getAsString();
                
                // Ensure correct directory serialization matching standard Mojang protocols
                String searchPrefix = basePath + folderName;
                
                Map<ResourceLocation, Resource> asset = resourceManager.listResources(searchPrefix,
                        loc -> loc.getNamespace().equals(namespace) && loc.getPath().endsWith(".json")
                );
                
                for (var entry : asset.entrySet()) {
                    try (Reader reader = entry.getValue().openAsReader()) {
                        JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
                        processAndRegister(loader, object);
                        
                    } catch (Exception e) {
                        logger.error("Failed to parse runtime JSON resource asset: {}", entry.getKey(), e);
                    }
                }
            }
        }
    }
    
    public static void reload() throws IOException {
        loadFolder(FileManager.getCinematicaFolder());
        loadProjects(Minecraft.getInstance().getResourceManager());
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends CinematicaAsset> T get(String name, Class<? extends CinematicaAssetLoader<T>> loaderType) {
        for (var entry : registry.getEntries()) {
            CinematicaAssetLoader<?> registeredLoader = entry.getValue();
            
            if (loaderType.isAssignableFrom(registeredLoader.getClass())) {
                CinematicaAssetLoader<T> castedLoader = (CinematicaAssetLoader<T>) registeredLoader;
                return castedLoader.get(name);
            }
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends CinematicaAsset> CinematicaAssetLoader<T> getLoader(Class<? extends CinematicaAssetLoader<T>> loaderClass) {
        for (var entry : registry.getEntries()) {
            CinematicaAssetLoader<?> registeredLoader = entry.getValue();
            
            if (loaderClass.isAssignableFrom(registeredLoader.getClass())) {
                return (CinematicaAssetLoader<T>) registeredLoader;
            }
        }
        
        return null;
    }
}