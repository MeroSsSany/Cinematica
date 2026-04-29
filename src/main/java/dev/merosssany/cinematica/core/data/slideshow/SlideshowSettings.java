package dev.merosssany.cinematica.core.data.slideshow;

import com.google.gson.*;
import dev.merosssany.cinematica.core.FileManager;
import dev.merosssany.cinematica.core.data.handler.FileAdapter;
import dev.merosssany.cinematica.core.data.handler.FileRelativeAdapter;
import dev.merosssany.cinematica.core.data.handler.Vector2fAdapter;
import dev.merosssany.cinematica.core.data.handler.Vector2iAdapter;
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
        KenBurnsOptions kenBurns,
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
        return new GsonBuilder()
                .registerTypeAdapter(Vector2i.class, new Vector2iAdapter())
                .registerTypeAdapter(Vector2f.class, new Vector2fAdapter())
                .registerTypeAdapter(File.class, new FileRelativeAdapter(root))
                .setPrettyPrinting()
                .create();
    }
    
    public static @NotNull Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Vector2i.class, new Vector2iAdapter())
                .registerTypeAdapter(Vector2f.class, new Vector2fAdapter())
                .registerTypeAdapter(File.class, new FileAdapter())
                .setPrettyPrinting()
                .create();
    }
    
    public JsonObject toJson() throws IOException {
        return toJson(FileManager.getCinematicaFolder().resolve(name));
    }
}
