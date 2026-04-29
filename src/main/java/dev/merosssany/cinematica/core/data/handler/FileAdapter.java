package dev.merosssany.cinematica.core.data.handler;

import com.google.gson.*;

import java.io.File;
import java.lang.reflect.Type;

public class FileAdapter implements JsonDeserializer<File>, JsonSerializer<File> {
    @Override
    public File deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new File(jsonElement.getAsString());
    }
    
    @Override
    public JsonElement serialize(File path, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(path.getPath());
    }
}
