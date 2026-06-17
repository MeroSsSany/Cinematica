package dev.merosssany.cinematica.core.data.loader.assets;

import com.google.gson.JsonObject;
import dev.merosssany.cinematica.core.data.death.DeathScreenSettings;
import dev.merosssany.cinematica.core.data.loader.CinematicaAssetLoader;
import dev.merosssany.cinematica.core.data.loader.Validation;
import dev.merosssany.cinematica.core.registry.settings.DeathScreenRegistry;
import dev.merosssany.cinematica.core.security.ObjectKey;

import java.util.Set;

import static dev.merosssany.cinematica.core.Cinematica.getGsonBuilder;

public class DeathScreenLoader implements CinematicaAssetLoader<DeathScreenSettings> {
    private final DeathScreenRegistry registry;
    
    public DeathScreenLoader(ObjectKey key) {
        registry = new DeathScreenRegistry(key);
    }
    
    @Override
    public Validation validate(JsonObject object) {
        if (!object.has("name") || !object.get("name").isJsonPrimitive()) {
            return new Validation(false, "Missing or invalid required field: 'name'");
        }
        
        return new Validation(true, "Success");
    }
    
    @Override
    public DeathScreenSettings createFromJson(JsonObject object) {
        return getGsonBuilder().create().fromJson(object, DeathScreenSettings.class);
    }
    
    @Override
    public void register(DeathScreenSettings resource) {
        registry.register(resource);
    }
    
    @Override
    public DeathScreenSettings get(String name) {
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