package dev.merosssany.cinematica.core.data.loader.assets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.merosssany.cinematica.core.data.loader.CinematicaAssetLoader;
import dev.merosssany.cinematica.core.data.loader.Validation;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import dev.merosssany.cinematica.core.registry.settings.SlideshowRegistry;
import dev.merosssany.cinematica.core.security.ObjectKey;

import java.util.Set;

public class SlideshowLoader implements CinematicaAssetLoader<SlideshowSettings> {
    private final SlideshowRegistry registry;
    
    public SlideshowLoader(ObjectKey key) {
        registry = new SlideshowRegistry(key);
    }
    
    @Override
    public Validation validate(JsonObject object) {
        if (!object.has("name") || !object.get("name").isJsonPrimitive()) {
            return new Validation(false, "Missing or invalid required field: 'name'");
        }
        
        if (!object.has("slides") || !object.get("slides").isJsonArray()) {
            return new Validation(false, "Missing or invalid required array: 'slides'");
        }
        
        JsonArray slidesArray = object.getAsJsonArray("slides");
        if (slidesArray.isEmpty()) {
            return new Validation(false, "The 'slides' array cannot be empty.");
        }
        
        if (object.has("offset")) {
            JsonElement offsetElement = object.get("offset");
            if (!offsetElement.isJsonArray() || offsetElement.getAsJsonArray().size() != 2) {
                return new Validation(false, "Field 'offset' must be a JSON array containing exactly 2 integers [x, y]");
            }
        }
        
        return new Validation(true, "Success");
    }
    
    @Override
    public SlideshowSettings createFromJson(JsonObject object) {
        return SlideshowSettings.fromJson(object);
    }
    
    @Override
    public void register(SlideshowSettings resource) {
        registry.register(resource);
    }
    
    @Override
    public SlideshowSettings get(String name) {
        return registry.get(name);
    }
    
    @Override
    public void freeze(ObjectKey key, boolean freeze) {
        registry.setFrozen(freeze, key);
    }
    
    @Override
    public Set<String> getRegistered() {
        return registry.getRegistered();
    }
}