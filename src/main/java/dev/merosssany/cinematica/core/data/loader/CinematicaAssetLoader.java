package dev.merosssany.cinematica.core.data.loader;

import com.google.gson.JsonObject;
import dev.merosssany.cinematica.core.data.CinematicaAsset;
import dev.merosssany.cinematica.core.security.ObjectKey;

import java.util.Set;

// T represents the specific asset this loader handles
public interface CinematicaAssetLoader<T extends CinematicaAsset> {
    Validation validate(JsonObject object);
    T createFromJson(JsonObject object);
    void register(T resource);
    T get(String name);
    void freeze(ObjectKey key, boolean freeze);
    Set<String> getRegistered();
}