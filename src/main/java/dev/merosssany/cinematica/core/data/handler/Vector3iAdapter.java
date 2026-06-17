package dev.merosssany.cinematica.core.data.handler;

import com.google.gson.*;
import org.joml.Vector3i;

import java.lang.reflect.Type;

public class Vector3iAdapter implements JsonSerializer<Vector3i>, JsonDeserializer<Vector3i> {
    @Override
    public JsonElement serialize(Vector3i src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        array.add(src.x);
        array.add(src.y);
        array.add(src.z);
        return array;
    }

    @Override
    public Vector3i deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        return new Vector3i(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
    }
}