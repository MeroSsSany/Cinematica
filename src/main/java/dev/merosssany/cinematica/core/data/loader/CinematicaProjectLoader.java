package dev.merosssany.cinematica.core.data.loader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.FileManager;
import dev.merosssany.cinematica.core.data.CinematicaAsset;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CinematicaProjectLoader {
    private static final ObjectKey key = new ObjectKey();
    private static final CinematicaAssetsRegistry registry = new CinematicaAssetsRegistry(key);
    private static final Logger logger = Cinematica.getLogger();
    
    public static void register(String name, CinematicaAssetLoader<?> cinematicaAssetLoader) {
        registry.register(name, cinematicaAssetLoader);
        logger.info("Registered asset loader extension for key: '{}'", name);
    }
    
    @SubscribeEvent
    public static void onLoadingComplete(FMLClientSetupEvent e) {
        logger.info("Freezing Cinematica asset registration layers...");
        registry.setFrozen(true, key);
    }
    
    public static CinematicaAssetsRegistry registry() {
        return registry;
    }
    
    public static void loadLocalProjects() throws IOException {
        Path localFolder = FileManager.getCinematicaFolder();
        logger.info("Scanning local directory for external project files: '{}'", localFolder);
        
        if (Files.isDirectory(localFolder)) {
            try (Stream<Path> stream = Files.list(localFolder)) {
                List<Path> list = stream.toList();
                logger.info("Found {} total sub-element(s) inside external appdata directory.", list.size());
                for (Path folder : list) {
                    loadFolder(folder);
                }
            }
        } else {
            logger.warn("Local appdata project directory folder does not exist or isn't a valid system directory.");
        }
    }
    
    public static void loadFolder(Path root) throws IOException {
        Path cinematica = root.resolve("cinematica.json");
        logger.debug("Checking external path folder for project signature: '{}'", root.toAbsolutePath());
        
        if (Files.exists(cinematica)) {
            logger.info("Discovered external project metadata signature at: '{}'", cinematica.toAbsolutePath());
            JsonObject config;
            try (FileReader reader = new FileReader(cinematica.toFile())) {
                config = JsonParser.parseReader(reader).getAsJsonObject();
            }
            
            if (config.has("folders")) {
                JsonElement foldersElement = config.get("folders");
                
                if (foldersElement.isJsonObject()) {
                    JsonObject folders = foldersElement.getAsJsonObject();
                    
                    for (Map.Entry<String, JsonElement> folderSet : folders.entrySet()) {
                        String pathKey = folderSet.getKey();
                        
                        if (!registry.isRegistered(pathKey)) {
                            logger.warn("Skipping directory parsing config map key '{}'—no corresponding java loader registered.", pathKey);
                            continue;
                        }
                        
                        CinematicaAssetLoader<?> asset = registry.get(pathKey);
                        String folderName = folderSet.getValue().getAsString();
                        Path resolvedPath = root.resolve(folderName);
                        
                        logger.info("Scanning local asset folder mapping: '{}' -> '{}'", pathKey, resolvedPath.toAbsolutePath());
                        
                        if (Files.isDirectory(resolvedPath)) {
                            int localAssetsRegistered = 0;
                            try (var stream = Files.list(resolvedPath)) {
                                for (Iterator<@NotNull Path> it = stream.iterator(); it.hasNext(); ) {
                                    Path file = it.next();
                                    if (!file.toString().endsWith(".json")) continue;
                                    
                                    logger.debug("Reading external project component configuration file: '{}'", file.getFileName());
                                    JsonObject object;
                                    try (Reader reader = new FileReader(file.toFile())) {
                                        object = JsonParser.parseReader(reader).getAsJsonObject();
                                    }
                                    processAndRegister(asset, object);
                                    localAssetsRegistered++;
                                }
                            }
                            logger.info("Mounted {} assets from local external folder: '{}'", localAssetsRegistered, folderName);
                        } else {
                            logger.warn("Asset configuration reference directory path map target '{}' does not exist as a sub-folder.", resolvedPath.toAbsolutePath());
                        }
                    }
                } else {
                    logger.error("External folder profile at '{}' has invalid configuration schema format: 'folders' must be an object map layer.", cinematica.toAbsolutePath());
                }
            }
        }
    }
    
    private static <T extends CinematicaAsset> void processAndRegister(CinematicaAssetLoader<T> loader, JsonObject object) {
        Validation validation = loader.validate(object);
        if (validation.success()) {
            T resource = loader.createFromJson(object);
            loader.register(resource);
            logger.info("Successfully loaded and registered cinematic asset: '{}'", resource.name());
        } else {
            logger.error("Validation failed for an asset configuration file! Reason: {}", validation.reason());
        }
    }
    
    public static void loadProjects(ResourceManager resourceManager) {
        logger.info("Commencing active Minecraft data/resource pack manifest file sweep...");
        
        Map<ResourceLocation, Resource> manifests = resourceManager.listResources("cinematica",
                loc -> loc.getPath().endsWith("cinematica.json")
        );
        
        logger.info("Pack Indexer Discovery: Identified {} active pack 'cinematica.json' manifests across layers.", manifests.size());
        
        for (Map.Entry<ResourceLocation, Resource> entry : manifests.entrySet()) {
            ResourceLocation manifestId = entry.getKey();
            Resource resource = entry.getValue();
            String namespace = manifestId.getNamespace();
            String fullPath = manifestId.getPath();
            
            String basePath = fullPath.substring(0, fullPath.length() - "cinematica.json".length());
            logger.info("Processing active layout profile manifest tree: {} [Namespace: '{}', Base Directory Path: '{}']", manifestId, namespace, basePath);
            
            try (Reader reader = resource.openAsReader()) {
                JsonObject metadataJson = JsonParser.parseReader(reader).getAsJsonObject();
                
                if (!metadataJson.has("version") || !metadataJson.has("folders")) {
                    logger.error("Skipping invalid pack project manifest specification: {}", manifestId);
                    continue;
                }
                
                JsonElement foldersElement = metadataJson.get("folders");
                if (!foldersElement.isJsonObject()) {
                    logger.error("Manifest at {} has an invalid structural schema format: 'folders' attribute mapping must be a JSON Object schema.", manifestId);
                    continue;
                }
                
                JsonObject folders = foldersElement.getAsJsonObject();
                loadPack(resourceManager, namespace, folders, basePath);
                logger.info("Successfully completely evaluated asset manifest pack branch from project namespace '{}'", namespace);
                
            } catch (Exception e) {
                logger.error("Critical error encountered while processing project resource manifest mapping at path {}", manifestId, e);
            }
        }
    }
    
    private static void loadPack(ResourceManager resourceManager, String namespace, JsonObject folders, String basePath) {
        for (var folderSet : folders.entrySet()) {
            String registryKey = folderSet.getKey();
            
            if (registry.isRegistered(registryKey)) {
                CinematicaAssetLoader<?> loader = registry.get(registryKey);
                String folderName = folderSet.getValue().getAsString();
                
                String searchPrefix = basePath + folderName;
                
                if (searchPrefix.endsWith("/")) {
                    searchPrefix = searchPrefix.substring(0, searchPrefix.length() - 1);
                }
                
                final String finalPrefix = searchPrefix;
                logger.debug("Scanning resource pack data catalog files tree route: '{}'", finalPrefix);
                
                Map<ResourceLocation, Resource> asset = resourceManager.listResources(finalPrefix,
                        loc -> loc.getNamespace().equals(namespace) && loc.getPath().endsWith(".json")
                );
                
                int registeredPackAssetsCount = 0;
                for (var entry : asset.entrySet()) {
                    logger.info("Discovered internal asset payload config entry: '{}'. Processing extraction...", entry.getKey());
                    try (Reader reader = entry.getValue().openAsReader()) {
                        JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
                        processAndRegister(loader, object);
                        registeredPackAssetsCount++;
                    } catch (Exception e) {
                        logger.error("Failed to parse runtime JSON resource pack asset specification: {}", entry.getKey(), e);
                    }
                }
                if (registeredPackAssetsCount > 0) {
                    logger.info(" -> Mounted {} pack items into registry category subsystem extension: '{}'", registeredPackAssetsCount, registryKey);
                }
            } else {
                logger.warn("Manifest config specifies a folder category binding '{}', but no mod registration asset loader supports this identifier.", registryKey);
            }
        }
    }
    
    public static void reload() throws IOException {
        logger.info("========================= CINEMATICA RELOAD START =========================");
        long startTime = System.currentTimeMillis();
        
        loadLocalProjects();
        loadProjects(Minecraft.getInstance().getResourceManager());
        
        long endTime = System.currentTimeMillis();
        logger.info("========================= CINEMATICA RELOAD COMPLETE ({}) =========================", String.format("%dms", endTime - startTime));
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
        logger.debug("Failed querying runtime engine asset tracking records for object named: '{}'", name);
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