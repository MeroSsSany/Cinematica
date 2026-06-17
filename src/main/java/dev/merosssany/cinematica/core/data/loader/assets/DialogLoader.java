package dev.merosssany.cinematica.core.data.loader.assets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.merosssany.cinematica.core.data.dialog.DialogSettings;
import dev.merosssany.cinematica.core.data.loader.CinematicaAssetLoader;
import dev.merosssany.cinematica.core.data.loader.Validation;
import dev.merosssany.cinematica.core.registry.settings.DialogRegistry;
import dev.merosssany.cinematica.core.security.ObjectKey;

import static dev.merosssany.cinematica.core.Cinematica.getGsonBuilder;

public class DialogLoader implements CinematicaAssetLoader<DialogSettings> {
    private final DialogRegistry registry;
    
    public DialogLoader(ObjectKey key) {
        this.registry = new DialogRegistry(key);
    }
    
    @Override
    public Validation validate(JsonObject object) {
        if (!object.has("name") || !object.get("name").isJsonPrimitive()) {
            return new Validation(false, "Missing or invalid required field: 'name'");
        }
        
        if (!object.has("dialogStages") || !object.get("dialogStages").isJsonArray()) {
            return new Validation(false, "Missing or invalid required array: 'dialogStages'");
        }
        
        JsonArray stagesArray = object.getAsJsonArray("dialogStages");
        if (stagesArray.isEmpty()) {
            return new Validation(false, "The 'dialogStages' array cannot be empty.");
        }
        
        return new Validation(true, "Success");
    }
    
    @Override
    public DialogSettings createFromJson(JsonObject object) {
        return getGsonBuilder().create().fromJson(object, DialogSettings.class);
    }
    
    @Override
    public void register(DialogSettings resource) {
        registry.register(resource);
    }
    
    @Override
    public DialogSettings get(String name) {
        return registry.get(name);
    }
    
    @Override
    public void freeze(ObjectKey key, boolean freeze) {
        registry.setFrozen(freeze, key);
    }
}