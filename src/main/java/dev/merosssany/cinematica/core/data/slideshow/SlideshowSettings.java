package dev.merosssany.cinematica.core.data.slideshow;

import com.google.gson.*;
import dev.merosssany.cinematica.core.FileManager;
import dev.merosssany.cinematica.core.data.RGBA;
import dev.merosssany.cinematica.core.data.handler.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public record SlideshowSettings(
        SlideshowSlide[] slides,
        boolean skippable,
        boolean alternateTextPosition,
        boolean largerTextBackground,
        int secondsToSwitch,
        float fadeSpeed,
        String name,
        Vector2i offset,
        File musicPath
) {
    public JsonObject toJson(Path root) {
        return getGson(root).toJsonTree(this).getAsJsonObject();
    }
    
    public static SlideshowSettings fromJson(JsonObject json, Path root) {
        Gson gson = getGson(root);
        
        return gson.fromJson(json, SlideshowSettings.class);
    }
    
    public static @NotNull Gson getGson(Path root) {
        return getGsonBuilder()
                .registerTypeAdapter(File.class, new FileRelativeAdapter(root))
                .create();
    }
    
    public static @NotNull GsonBuilder getGsonBuilder() {
        return new GsonBuilder()
                .registerTypeAdapter(Vector2i.class, new Vector2iAdapter())
                .registerTypeAdapter(Vector2f.class, new Vector2fAdapter())
                .registerTypeAdapter(RGBA.class, new RgbaAdaptor())
                .setPrettyPrinting();
    }
    
    public static Gson getGson() {
        return getGsonBuilder()
                .registerTypeAdapter(File.class, new FileAdapter())
                .create();
    }
    
    public JsonObject toJson() throws IOException {
        return toJson(FileManager.getCinematicaFolder().resolve(name));
    }
}
