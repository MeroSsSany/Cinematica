package dev.merosssany.cinematica.core.data.handler;

import com.google.gson.*;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.lang.reflect.Type;

public class Vector2fAdapter implements JsonSerializer<Vector2f>, JsonDeserializer<Vector2f> {
    @Override
    public JsonElement serialize(Vector2f src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        array.add(src.x);
        array.add(src.y);
        return array;
    }

    @Override
    public Vector2f deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        return new Vector2f(array.get(0).getAsFloat(), array.get(1).getAsFloat());
    }
}