package dev.merosssany.cinematica.core.data.handler;

import com.google.gson.*;
import org.joml.Vector2i;

import java.lang.reflect.Type;

public class Vector2iAdapter implements JsonSerializer<Vector2i>, JsonDeserializer<Vector2i> {
    @Override
    public JsonElement serialize(Vector2i src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        array.add(src.x);
        array.add(src.y);
        return array;
    }

    @Override
    public Vector2i deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        return new Vector2i(array.get(0).getAsInt(), array.get(1).getAsInt());
    }
}