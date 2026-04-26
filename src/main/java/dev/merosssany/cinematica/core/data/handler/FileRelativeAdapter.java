package dev.merosssany.cinematica.core.data.handler;

import com.google.gson.*;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;

public class FileRelativeAdapter implements JsonSerializer<File>, JsonDeserializer<File> {
    private final Path root;

    public FileRelativeAdapter(Path root) { this.root = root; }

    @Override
    public JsonElement serialize(File src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(root.relativize(src.toPath()).toString());
    }

    @Override
    public File deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return root.resolve(json.getAsString()).toFile();
    }
}