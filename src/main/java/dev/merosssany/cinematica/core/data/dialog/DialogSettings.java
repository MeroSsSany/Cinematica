package dev.merosssany.cinematica.core.data.dialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.merosssany.cinematica.core.InvalidJsonException;

import java.nio.file.Path;

public record DialogSettings(
        String dialogName,
        int speed,
        boolean skippable,
        boolean overlayMode,
        DialogStage[] dialogStages,
        Path root
) {
    
    public static DialogSettings fromJson(JsonObject json, Path root) throws InvalidJsonException {
        if (!isValid(json)) {
            throw new InvalidJsonException("Invalid Dialog JSON: Missing mandatory 'name' or 'stages' fields.");
        }
        
        // Handle the Array of Stages
        JsonArray stagesArray = json.getAsJsonArray("stages");
        DialogStage[] stages = new DialogStage[stagesArray.size()];
        for (int i = 0; i < stagesArray.size(); i++) {
            stages[i] = DialogStage.fromJson(stagesArray.get(i).getAsJsonObject());
        }
        
        return new DialogSettings(
                json.get("name").getAsString(),
                json.has("speed") ? json.get("speed").getAsInt() : 20,
                json.has("skippable") && json.get("skippable").getAsBoolean(),
                json.has("has_options") && json.get("has_options").getAsBoolean(),
                stages,
                root
        );
    }
    
    public static boolean isValid(JsonObject json) {
        // Name is mandatory to identify the dialog, and stages are mandatory to have a conversation
        return json.has("name") && json.has("stages") && json.get("stages").isJsonArray();
    }
    
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", dialogName);
        json.addProperty("speed", speed);
        json.addProperty("skippable", skippable);
        json.addProperty("overlay_mode", overlayMode);
        
        JsonArray stagesArray = new JsonArray();
        for (DialogStage stage : dialogStages) {
            stagesArray.add(stage.toJson());
        }
        json.add("stages", stagesArray);
        
        return json;
    }
}