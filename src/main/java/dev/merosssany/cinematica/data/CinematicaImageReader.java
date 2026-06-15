package dev.merosssany.cinematica.data;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.rendering.TextureInfo;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.FileInputStream;
import java.io.IOException;

public class CinematicaImageReader {
    public static TextureInfo read(String path, String locationName) throws IOException {
        try (FileInputStream stream = new FileInputStream(path)) {
            // NativeImage reads the raw stream data cleanly
            NativeImage image = NativeImage.read(stream);
            
            float imgW = image.getWidth();
            float imgH = image.getHeight();
            
            DynamicTexture texture = new DynamicTexture(image);
            
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Cinematica.MODID, locationName);
            
            Minecraft.getInstance().getTextureManager().register(location, texture);
            
            return new TextureInfo(imgW, imgH, texture, location);
        }
    }
}