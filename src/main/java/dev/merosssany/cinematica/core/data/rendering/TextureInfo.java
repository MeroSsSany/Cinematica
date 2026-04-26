package dev.merosssany.cinematica.core.data.rendering;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

public record TextureInfo(float width, float height, DynamicTexture texture, ResourceLocation location) {
    public static TextureInfo fromResourceLocation(ResourceLocation location) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(location);
        int width, height;
        DynamicTexture dTex = null;
        
        if (texture != null) {
            if (texture instanceof DynamicTexture dynamicTexture) {
                NativeImage image = dynamicTexture.getPixels();
                width = image.getWidth();
                height = image.getHeight();
                dTex = dynamicTexture;
                
            } else {
                int textureId = texture.getId();
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
                
                width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
                height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
                
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            }
            return new TextureInfo(width, height, dTex, location);
        }
        return null;
    }
    
    public static TextureInfo empty() {
        return new TextureInfo(0,0,null,null);
    }
    
    public boolean isEmpty() {
        return texture == null && location == null;
    }
}
