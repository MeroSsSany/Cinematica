package dev.merosssany.cinematica.core.data.dialog;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public record DialogStage(
        String name,
        String entityType,
        String texture,
        String text,
        String[] options,
        int color,
        int time,
        boolean useTexture,
        boolean useTextInput
) {
    
    public static DialogStage fromJson(JsonObject json) {
        return new Gson().fromJson(json, DialogStage.class);
    }
    
    public JsonObject toJson() {
        return new Gson().toJsonTree(this).getAsJsonObject();
    }
}