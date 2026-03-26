package dev.merosssany.cinematica.core.data.intro;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.joml.Vector2f;

import java.io.File;
import java.nio.file.Path;

public record SlideshowSlide(
        String title,
        String subtext,
        boolean isImage,
        File assetPath, // Renamed from imagePath for consistency
        Vector2f anchor
) {
    public static SlideshowSlide fromJson(JsonObject json, Path root) {
        Vector2f anchor = new Vector2f();
        JsonArray anchorArr = json.getAsJsonArray("anchor");
        
        if (anchorArr != null && anchorArr.size() >= 2) {
            anchor.set(anchorArr.get(0).getAsFloat(), anchorArr.get(1).getAsFloat());
        }
        
        return new SlideshowSlide(
                json.get("title").getAsString(),
                json.get("subtext").getAsString(),
                json.get("isImage").getAsBoolean(),
                root.resolve(json.get("assetPath").getAsString()).toFile(),
                anchor
        );
    }
    
    public JsonObject toJson(Path root) {
        JsonObject object = new JsonObject();
        JsonArray anchorArr = new JsonArray();
        
        anchorArr.add(anchor.x);
        anchorArr.add(anchor.y);
        
        object.addProperty("title", title);
        object.addProperty("subtext", subtext);
        object.addProperty("isImage", isImage);
        object.addProperty("assetPath", root.relativize(assetPath.toPath()).toString());
        object.add("anchor", anchorArr);
        
        return object;
    }
}
