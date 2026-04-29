package dev.merosssany.cinematica.core.data.dialog;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.nio.file.Path;

public record DialogSettings(
        String dialogName,
        int speed,
        boolean skippable,
        boolean overlayMode,
        DialogStage[] dialogStages,
        Path root
) {
    
    public static DialogSettings fromJson(JsonObject json) {
        return new Gson().fromJson(json, DialogSettings.class);
    }
    
    public JsonObject toJson() {
        return new Gson().toJsonTree(this).getAsJsonObject();
    }
}