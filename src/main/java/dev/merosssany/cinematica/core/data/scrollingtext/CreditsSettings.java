package dev.merosssany.cinematica.core.data.scrollingtext;

import com.google.gson.JsonObject;
import dev.merosssany.cinematica.core.data.CinematicaAsset;

import static dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings.getGson;

public record CreditsSettings(
    boolean wave,
    float speed,
    float scale,
    float fadeSpeed,
    String text,
    String logo,
    String music,
    String finalMessage,
    String name
) implements CinematicaAsset {
    public static boolean isValid(JsonObject j) {
        return j.has("text") && j.has("name");
    }
    
    public JsonObject toJson() {
        return getGson().toJsonTree(this).getAsJsonObject();
    }
    
    public static CreditsSettings fromJson(JsonObject json) {
        return getGson().fromJson(json, CreditsSettings.class);
    }
}
