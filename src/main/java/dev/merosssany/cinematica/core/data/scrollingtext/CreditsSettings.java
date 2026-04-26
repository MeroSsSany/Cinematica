package dev.merosssany.cinematica.core.data.scrollingtext;

import com.google.gson.JsonObject;

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
) {
    public static boolean isValid(JsonObject j) {
        return j.has("text") && j.has("dialogName");
    }
    
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        
        object.addProperty("wave", wave);
        object.addProperty("speed", speed);
        object.addProperty("text", text);
        object.addProperty("logo", logo);
        object.addProperty("music", music);
        object.addProperty("finalMessage", finalMessage);
        object.addProperty("scale", scale);
        object.addProperty("fadeSpeed", fadeSpeed);
        object.addProperty("dialogName", name);
        
        return object;
    }
    
    public static CreditsSettings fromJson(JsonObject json) {
        if (!isValid(json)) return null;
        
        boolean wave = true;
        float speed = 20, scale = 2, fadeSpeed = 2;
        String logo = "", music = "", finalMessage = "";
        
        if (json.has("wave")) wave = json.get("wave").getAsBoolean();
        if (json.has("speed")) speed = json.get("speed").getAsFloat();
        if (json.has("scale")) scale = json.get("scale").getAsFloat();
        if (json.has("fadeSpeed")) fadeSpeed = json.get("fadeSpeed").getAsFloat();
        if (json.has("logo")) logo = json.get("logo").getAsString();
        if (json.has("music")) music = json.get("music").getAsString();
        if (json.has("finalMessage")) finalMessage = json.get("finalMessage").getAsString();
        
        return new CreditsSettings(
                wave,
                speed,
                scale,
                fadeSpeed,
                json.get("text").getAsString(),
                logo,
                music,
                finalMessage,
                json.get("dialogName").getAsString()
        );
    }
}
