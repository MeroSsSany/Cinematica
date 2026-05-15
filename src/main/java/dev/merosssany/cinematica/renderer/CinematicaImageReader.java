package dev.merosssany.cinematica.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.rendering.TextureInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.FileInputStream;
import java.io.IOException;

public class CinematicaImageReader {
    public static TextureInfo read(String path, String locationName) throws IOException {
        try (FileInputStream stream = new FileInputStream(path)) {
            NativeImage image = NativeImage.read(stream);
            DynamicTexture texture = new DynamicTexture(image);
            
            float imgW = image.getWidth();
            float imgH = image.getHeight();
            
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Cinematica.MODID, locationName);
            image.close();
            
            Minecraft.getInstance().getTextureManager().register(location, texture);
            
            return new TextureInfo(imgW, imgH, texture, location);
        }
    }
}
