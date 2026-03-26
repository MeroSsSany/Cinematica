package dev.merosssany.cinematica.core.data.intro;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.joml.Vector2i;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public record SlideshowSettings(
        SlideshowSlide[] stages,
        boolean skippable,
        boolean alternateTextPosition,
        boolean bottomView,
        boolean kenBurns,
        int secondsToSwitch,
        float fadeSpeed,
        String name,
        Vector2i offset,
        File musicPath
) {
    public JsonObject toJson(Path root) {
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();
        JsonArray offsetArr = new JsonArray();
        
        for (SlideshowSlide stage : stages) {
            array.add(stage.toJson(root));
        }
        
        offsetArr.add(offset.x);
        offsetArr.add(offset.y);
        
        object.add("stages", array);
        object.add("offset", offsetArr);
        
        object.addProperty("skippable", skippable);
        object.addProperty("biggerTextBackground", bottomView);
        object.addProperty("secondsToSwitch", secondsToSwitch);
        object.addProperty("kenBurns", kenBurns);
        object.addProperty("name", name);
        object.addProperty("alternateTextPosition", alternateTextPosition);
        object.addProperty("musicPath", root.relativize(musicPath.toPath()).toString());
        object.addProperty("fadeSpeed", fadeSpeed);
        
        return object;
    }
    
    public static SlideshowSettings fromJson(JsonObject json, Path root) {
        List<JsonElement> stagesArr = json.getAsJsonArray("stages").asList();
        JsonArray offsetArr = json.getAsJsonArray("offset");
        Vector2i offset = new Vector2i();
        
        SlideshowSlide[] stages = new SlideshowSlide[stagesArr.size()];
        for (int i = 0; i < stages.length; i++) {
            stages[i] = SlideshowSlide.fromJson(stagesArr.get(i).getAsJsonObject(),root);
        }
        
        if (offsetArr != null && offsetArr.size() >= 2) {
            offset.set(offsetArr.get(0).getAsInt(), offsetArr.get(1).getAsInt());
        }
        
        return new SlideshowSettings(
                stages,
                json.get("skippable").getAsBoolean(),
                json.get("alternateTextPosition").getAsBoolean(),
                json.get("biggerTextBackground").getAsBoolean(),
                json.get("kenBurns").getAsBoolean(),
                json.get("secondsToSwitch").getAsInt(),
                json.get("fadeSpeed").getAsFloat(),
                json.get("name").getAsString(),
                offset,
                root.resolve(json.get("musicPath").getAsString()).toFile()
        );
    }
}
