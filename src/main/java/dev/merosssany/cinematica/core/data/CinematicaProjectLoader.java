package dev.merosssany.cinematica.core.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.death.DeathScreenSettings;
import dev.merosssany.cinematica.core.data.scrollingtext.CreditsSettings;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import dev.merosssany.cinematica.core.security.ObjectKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.Map;

import static dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings.getGson;

public class CinematicaProjectLoader {
    private static final Logger logger = LogUtils.getLogger();
    private static ObjectKey key;
    
    public static void key(ObjectKey key) {
        if (CinematicaProjectLoader.key == null) CinematicaProjectLoader.key = key;
    }
    
    public static void reloadProjects(ResourceManager resourceManager) {
        Cinematica.beginReload(key);
        
        // Scan for any cinematica.json file sitting inside a 'cinematica' namespace/folder layout
        Map<ResourceLocation, Resource> manifests = resourceManager.listResources("cinematica",
                loc -> loc.getPath().endsWith("cinematica.json")
        );
        
        for (Map.Entry<ResourceLocation, Resource> entry : manifests.entrySet()) {
            ResourceLocation manifestId = entry.getKey();
            Resource resource = entry.getValue();
            String namespace = manifestId.getNamespace();
            String fullPath = manifestId.getPath(); // e.g., "cinematica/cinematica.json"
            
            // Extract the base path dynamically.
            // This leaves us with "cinematica/" (including the trailing slash)
            String basePath = fullPath.substring(0, fullPath.length() - "cinematica.json".length());
            
            try (Reader reader = resource.openAsReader()) {
                JsonObject metadataJson = JsonParser.parseReader(reader).getAsJsonObject();
                
                if (!metadataJson.has("version") || !metadataJson.has("folders")) {
                    logger.error("Skipping invalid pack project manifest: {}", manifestId);
                    continue;
                }
                
                JsonObject folders = metadataJson.getAsJsonObject("folders");
                
                // Pass the dynamically calculated basePath down to the loader logic
                loadPack(resourceManager, namespace, folders, basePath);
                logger.info("Successfully loaded Cinematica pack project from namespace '{}' using base path '{}'", namespace, basePath);
                
            } catch (Exception e) {
                logger.error("Failed to process Cinematica project manifest at {}", manifestId, e);
            }
        }
    }
    
    private static void loadPack(ResourceManager resourceManager, String namespace, JsonObject folders, String basePath) {
        if (folders.has(Cinematica.SLIDESHOWS)) loadSlideshow(resourceManager, namespace, folders, basePath);
        if (folders.has(Cinematica.SCROLLING_TEXT)) loadCreditScreen(resourceManager, namespace, folders, basePath);
        if (folders.has(Cinematica.DEATH_SCREEN)) loadDeathScreen(resourceManager, namespace, folders, basePath);
    }
    
    private static void loadSlideshow(ResourceManager resourceManager, String namespace, JsonObject folders, String basePath) {
        String folderName = folders.get(Cinematica.SLIDESHOWS).getAsString();
        
        // Combines base path and folder name to form: "cinematica/cinematica/slideshows"
        String searchPrefix = basePath + folderName;
        
        Map<ResourceLocation, Resource> slideshowConfigs = resourceManager.listResources(searchPrefix,
                loc -> loc.getNamespace().equals(namespace) && loc.getPath().endsWith(".json")
        );
        
        for (Map.Entry<ResourceLocation, Resource> entry : slideshowConfigs.entrySet()) {
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                SlideshowSettings settings = SlideshowSettings.fromJson(json);
                Cinematica.register(settings, null);
                
            } catch (Exception e) {
                logger.error("Failed to parse slideshow configuration inside pack mapping: {}", entry.getKey(), e);
            }
        }
    }
    
    private static void loadDeathScreen(ResourceManager resourceManager, String namespace, JsonObject folders, String basePath) {
        String folderName = folders.get(Cinematica.DEATH_SCREEN).getAsString();
        String searchPrefix = basePath + folderName;
        
        Map<ResourceLocation, Resource> configs = resourceManager.listResources(
                searchPrefix,
                location -> location.getNamespace().equals(namespace) && location.getPath().endsWith(".json")
        );
        
        for (Map.Entry<ResourceLocation, Resource> entry : configs.entrySet()) {
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                DeathScreenSettings settings = getGson().fromJson(jsonObject, DeathScreenSettings.class);
                Cinematica.deathScreenRegistry().register(settings);
            } catch (Exception e) {
                logger.error("Failed to parse death screen configuration inside pack mapping: {}", entry.getKey(), e);
            }
        }
    }
    
    private static void loadCreditScreen(ResourceManager resourceManager, String namespace, JsonObject folders, String basePath) {
        String folderName = folders.get(Cinematica.SCROLLING_TEXT).getAsString();
        String searchPrefix = basePath + folderName + "/";
        
        Map<ResourceLocation, Resource> configs = resourceManager.listResources(
                searchPrefix,
                location -> location.getNamespace().equals(namespace) && location.getPath().endsWith(".json")
        );
        
        for (Map.Entry<ResourceLocation, Resource> entry : configs.entrySet()) {
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                CreditsSettings settings = CreditsSettings.fromJson(jsonObject);
                Cinematica.creditScreenRegistry().register(settings);
            } catch (Exception e) {
                logger.error("Failed to parse credits screen configuration inside pack mapping: {}", entry.getKey(), e);
            }
        }
    }
}