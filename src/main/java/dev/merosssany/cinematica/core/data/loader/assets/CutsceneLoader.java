package dev.merosssany.cinematica.core.data.loader.assets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.merosssany.cinematica.core.data.cutscene.CutsceneSettings;
import dev.merosssany.cinematica.core.data.loader.CinematicaAssetLoader;
import dev.merosssany.cinematica.core.data.loader.Validation;
import dev.merosssany.cinematica.core.registry.settings.CutsceneRegistry;
import dev.merosssany.cinematica.core.security.ObjectKey;

import java.util.Set;

import static dev.merosssany.cinematica.core.Cinematica.getGsonBuilder;

public class CutsceneLoader implements CinematicaAssetLoader<CutsceneSettings> {
    private final CutsceneRegistry registry;
    
    public CutsceneLoader(ObjectKey key) {
        registry = new CutsceneRegistry(key);
    }
    
    @Override
    public Validation validate(JsonObject object) {
        if (!object.has("name") || !object.get("name").isJsonPrimitive()) {
            return new Validation(false, "Missing or invalid required field: 'name'");
        }
        
        if (!object.has("frames") || !object.get("frames").isJsonArray()) {
            return new Validation(false, "Missing or invalid required array: 'frames'");
        }
        
        JsonArray framesArray = object.getAsJsonArray("frames");
        if (framesArray.isEmpty()) {
            return new Validation(false, "The 'frames' array cannot be empty.");
        }
        
        return new Validation(true, "Success");
    }
    
    @Override
    public CutsceneSettings createFromJson(JsonObject object) {
        return getGsonBuilder().create().fromJson(object, CutsceneSettings.class);
    }
    
    @Override
    public void register(CutsceneSettings resource) {
        registry.register(resource);
    }
    
    @Override
    public CutsceneSettings get(String name) {
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