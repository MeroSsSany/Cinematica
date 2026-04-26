package dev.merosssany.cinematica.core.data.dialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.merosssany.cinematica.core.InvalidJsonException;

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
    
    public static DialogStage fromJson(JsonObject json) throws InvalidJsonException {
        if (!isValid(json)) {
            throw new InvalidJsonException("Invalid DialogStage JSON: Missing 'name' or 'text'.");
        }
        
        // Handle Options Array
        String[] options = new String[0];
        if (json.has("options") && json.get("options").isJsonArray()) {
            JsonArray array = json.getAsJsonArray("options");
            options = new String[array.size()];
            for (int i = 0; i < array.size(); i++) {
                options[i] = array.get(i).getAsString();
            }
        }
        
        return new DialogStage(
                json.get("name").getAsString(),
                json.has("entity_type") ? json.get("entity_type").getAsString() : "minecraft:pig",
                json.has("texture") ? json.get("texture").getAsString() : "",
                json.get("text").getAsString(),
                options,
                json.has("color") ? json.get("color").getAsInt() : 0xFFFFFF,
                json.has("time") ? json.get("time").getAsInt() : 0, // 0 could mean infinite/manual skip
                json.has("use_texture") && json.get("use_texture").getAsBoolean(),
                json.has("use_text_input") && json.get("use_text_input").getAsBoolean()
        );
    }
    
    public static boolean isValid(JsonObject json) {
        return json.has("name") && json.has("text");
    }
    
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("entity_type", entityType);
        json.addProperty("texture", texture);
        json.addProperty("text", text);
        
        JsonArray optionsArray = new JsonArray();
        for (String option : options) {
            optionsArray.add(option);
        }
        json.add("options", optionsArray);
        
        json.addProperty("color", color);
        json.addProperty("time", time);
        json.addProperty("use_texture", useTexture);
        json.addProperty("use_text_input", useTextInput);
        return json;
    }
}