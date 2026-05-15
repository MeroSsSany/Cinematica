package dev.merosssany.cinematica.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.merosssany.cinematica.core.data.RGBA;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class Renderer {
    private static final ResourceLocation VIGNETTE_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");
    
    public static int getLength(FormattedCharSequence charSequence) {
        int[] l = {0};
        charSequence.accept((index, style, codepoint) -> {
            l[0]++;
            return true;
        });
        return l[0];
    }
    
    public static OverflowData layoutText(Font font, List<String> text, int screenWidth) {
        List<FormattedCharSequence> wrappedLines = new ArrayList<>();
        int maxWidth = screenWidth - 32;
        int fontHeight = font.lineHeight + 1;
        int totalHeight;
        
        for (String originalLine : text) {
            List<FormattedCharSequence> splitLines = font.split(Component.literal(originalLine), maxWidth);
            
            wrappedLines.addAll(splitLines);
        }
        
        totalHeight = wrappedLines.size() * fontHeight;
        
        int[] length = new int[wrappedLines.size()];
        for (int i = 0; i < length.length; i++) {
            length[i] = getLength(wrappedLines.get(i));
        }
        
        return new OverflowData(wrappedLines, totalHeight, length);
    }
    
    public static void drawScaledString(Font font, GuiGraphics graphics, String text, int x, int y, float scale, int color, boolean center) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 100);
        graphics.pose().scale(scale, scale, 1.0f);
        
        if (center) {
            graphics.drawCenteredString(font, text, 0, 0, color);
        } else {
            graphics.drawString(font, text, 0, 0, color, true);
        }
        
        graphics.pose().popPose();
    }
    
    public static void drawVignette(GuiGraphics graphics, int width, int height, RGBA color) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        
        RenderSystem.enableBlend();
        
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ZERO,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        
        graphics.setColor(
                color.r(),
                color.g(),
                color.b(),
                color.a()
        );
        
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 450);
        
        graphics.blit(
                VIGNETTE_LOCATION,
                0, 0,
                0, 0,
                width, height,
                width, height
        );
        
        graphics.pose().popPose();
        
        graphics.setColor(1f, 1f, 1f, 1f);
        
        RenderSystem.defaultBlendFunc();
        
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
