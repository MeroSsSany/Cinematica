package dev.merosssany.cinematica.core.data.slideshow;

import com.google.gson.*;
import dev.merosssany.cinematica.core.data.RGBA;
import dev.merosssany.cinematica.core.data.handler.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.io.File;
import java.io.IOException;

public record SlideshowSettings(
        SlideshowSlide[] slides,
        boolean skippable,
        boolean alternateTextPosition,
        boolean largerTextBackground,
        float fadeSpeed,
        String name,
        Vector2i offset,
        String musicPath
) {
    public static SlideshowSettings fromJson(JsonObject json) {
        Gson gson = getGson();
        
        return gson.fromJson(json, SlideshowSettings.class);
    }
    
    public static @NotNull GsonBuilder getGsonBuilder() {
        return new GsonBuilder()
                .registerTypeAdapter(Vector2i.class, new Vector2iAdapter())
                .registerTypeAdapter(Vector2f.class, new Vector2fAdapter())
                .registerTypeAdapter(RGBA.class, new RgbaAdaptor())
                .setPrettyPrinting();
    }
    
    public static Gson getGson() {
        return getGsonBuilder()
                .registerTypeAdapter(File.class, new FileAdapter())
                .create();
    }
    
    public JsonObject toJson() throws IOException {
        return getGson().toJsonTree(this).getAsJsonObject();
    }
    
    public Component toComponentJson() throws IOException {
        JsonObject json = toJson();
        return formatElement(json, 0);
    }
    
    private Component formatElement(JsonElement element, int indent) {
        String spacing = "  ".repeat(indent);
        
        if (element.isJsonObject()) {
            MutableComponent comp = Component.literal("{\n");
            JsonObject obj = element.getAsJsonObject();
            var entries = obj.entrySet().stream().toList();
            
            for (int i = 0; i < entries.size(); i++) {
                var entry = entries.get(i);
                comp.append(Component.literal(spacing + "  \"").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(entry.getKey()).withStyle(ChatFormatting.GOLD))
                        .append(Component.literal("\": ").withStyle(ChatFormatting.GRAY))
                        .append(formatElement(entry.getValue(), indent + 1));
                
                if (i < entries.size() - 1) comp.append(Component.literal(",").withStyle(ChatFormatting.GRAY));
                comp.append(Component.literal("\n"));
            }
            return comp.append(Component.literal(spacing + "}").withStyle(ChatFormatting.GRAY));
        }
        
        if (element.isJsonArray()) {
            MutableComponent comp = Component.literal("[\n");
            JsonArray array = element.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                comp.append(Component.literal(spacing + "  "))
                        .append(formatElement(array.get(i), indent + 1));
                if (i < array.size() - 1) comp.append(Component.literal(",").withStyle(ChatFormatting.GRAY));
                comp.append(Component.literal("\n"));
            }
            return comp.append(Component.literal(spacing + "]").withStyle(ChatFormatting.GRAY));
        }
        
        // Primitives
        if (element.isJsonPrimitive()) {
            JsonPrimitive prim = element.getAsJsonPrimitive();
            if (prim.isNumber()) return Component.literal(prim.toString()).withStyle(ChatFormatting.AQUA);
            if (prim.isBoolean()) return Component.literal(prim.toString()).withStyle(ChatFormatting.LIGHT_PURPLE);
            return Component.literal("\"" + prim.getAsString() + "\"").withStyle(ChatFormatting.GREEN);
        }
        
        return Component.literal("null").withStyle(ChatFormatting.RED);
    }
}
