package dev.merosssany.cinematica.core.data.handler;

import com.google.gson.*;
import dev.merosssany.cinematica.core.data.RGBA;

import java.lang.reflect.Type;

public class RgbaAdaptor implements JsonSerializer<RGBA>, JsonDeserializer<RGBA> {
    @Override
    public RGBA deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        
        return RGBA.fromRGBA(
                object.get("red").getAsInt(),
                object.get("green").getAsInt(),
                object.get("blue").getAsInt(),
                object.get("alpha").getAsFloat()
        );
    }
    
    @Override
    public JsonElement serialize(RGBA rgba, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = new JsonObject();
        
        object.addProperty("red", rgba.getHexRed());
        object.addProperty("green", rgba.getHexGreen());
        object.addProperty("blue", rgba.getHexBlue());
        object.addProperty("alpha", rgba.a());
        
        return object;
    }
}
