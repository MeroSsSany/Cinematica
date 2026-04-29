package dev.merosssany.cinematica.core.data.dialog;

import com.google.gson.JsonObject;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;

import java.nio.file.Path;

public record DialogSettings(
        String dialogName,
        int speed,
        boolean skippable,
        boolean overlayMode,
        DialogStage[] dialogStages
) {
    
    public static DialogSettings fromJson(JsonObject json, Path root) {
        return SlideshowSettings.getGson(root).fromJson(json, DialogSettings.class);
    }
    
    public JsonObject toJson(Path root) {
        return SlideshowSettings.getGson(root).toJsonTree(this).getAsJsonObject();
    }
}