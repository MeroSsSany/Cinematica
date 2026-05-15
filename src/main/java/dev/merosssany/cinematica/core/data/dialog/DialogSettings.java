package dev.merosssany.cinematica.core.data.dialog;

import com.google.gson.JsonObject;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;

public record DialogSettings(
        String dialogName,
        int speed,
        boolean skippable,
        boolean overlayMode,
        DialogStage[] dialogStages
) {
    
    public static DialogSettings fromJson(JsonObject json) {
        return SlideshowSettings.getGson().fromJson(json, DialogSettings.class);
    }
    
    public JsonObject toJson() {
        return SlideshowSettings.getGson().toJsonTree(this).getAsJsonObject();
    }
}