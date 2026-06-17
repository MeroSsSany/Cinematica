package dev.merosssany.cinematica.core.data.loader.assets;

import com.google.gson.JsonObject;
import dev.merosssany.cinematica.core.data.loader.CinematicaAssetLoader;
import dev.merosssany.cinematica.core.data.loader.Validation;
import dev.merosssany.cinematica.core.data.scrollingtext.CreditsSettings;
import dev.merosssany.cinematica.core.registry.settings.CreditScreenRegistry;
import dev.merosssany.cinematica.core.security.ObjectKey;

import java.util.Set;

import static dev.merosssany.cinematica.core.Cinematica.getGsonBuilder;

public class CreditsLoader implements CinematicaAssetLoader<CreditsSettings> {
    private final CreditScreenRegistry registry;
    
    public CreditsLoader(ObjectKey key) {
        registry = new CreditScreenRegistry(key);
    }
    
    @Override
    public Validation validate(JsonObject object) {
        if (!object.has("name") || !object.get("name").isJsonPrimitive()) {
            return new Validation(false, "Missing or invalid required field: 'name'");
        }
        
        if (!object.has("text") || !object.get("text").isJsonPrimitive()) {
            return new Validation(false, "Missing or invalid required field: 'text' (The text payload to scroll must be a string).");
        }
        
        return new Validation(true, "Success");
    }
    
    @Override
    public CreditsSettings createFromJson(JsonObject object) {
        return getGsonBuilder().create().fromJson(object, CreditsSettings.class);
    }
    
    @Override
    public void register(CreditsSettings resource) {
        registry.register(resource.name(), resource);
    }
    
    @Override
    public CreditsSettings get(String name) {
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