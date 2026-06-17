package dev.merosssany.cinematica.core.data.handler;

import com.google.gson.*;
import org.joml.Vector3d;

import java.lang.reflect.Type;

public class Vector3dAdapter implements JsonSerializer<Vector3d>, JsonDeserializer<Vector3d> {
    @Override
    public JsonElement serialize(Vector3d src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        array.add(src.x);
        array.add(src.y);
        array.add(src.z);
        return array;
    }

    @Override
    public Vector3d deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        return new Vector3d(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
    }
}