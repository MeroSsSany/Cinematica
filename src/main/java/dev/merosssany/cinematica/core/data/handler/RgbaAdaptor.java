package dev.merosssany.cinematica.core.data.handler;

import com.google.gson.*;
import dev.merosssany.cinematica.core.data.RGBA;

import java.lang.reflect.Type;

public class RgbaAdaptor implements JsonSerializer<RGBA>, JsonDeserializer<RGBA> {
    @Override
    public RGBA deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonArray object = jsonElement.getAsJsonArray();
        
        return RGBA.fromRGBA(
                object.get(0).getAsInt(),
                object.get(1).getAsInt(),
                object.get(2).getAsInt(),
                object.get(3).getAsInt()
        );
    }
    
    @Override
    public JsonElement serialize(RGBA rgba, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonArray object = new JsonArray();
        
        object.add(rgba.getHexRed());
        object.add(rgba.getHexGreen());
        object.add(rgba.getHexBlue());
        object.add(rgba.getHexAlpha());
        
        return object;
    }
}
