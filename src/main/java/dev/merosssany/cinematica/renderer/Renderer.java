package dev.merosssany.cinematica.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.merosssany.cinematica.core.data.RGBA;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
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
        
        for (String originalLine : text) {
            List<FormattedCharSequence> splitLines = font.split(Component.literal(originalLine), maxWidth);
            wrappedLines.addAll(splitLines);
        }
        
        int totalHeight = wrappedLines.size() * fontHeight;
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
        
        // This tells OpenGL to treat the white values (1.0) as full color/opacity
        // and black values (0.0) as transparent/empty space.
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
        
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(color.r(), color.g(), color.b(), color.a());
        graphics.setColor(color.r(), color.g(), color.b(), color.a());
        
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
        
        // Reset pipeline back to standard vanilla defaults completely
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
    
    /**
     * Splits computation steps into safe Horizontal and Vertical tracks.
     * Uses FastColor architecture to ensure platform cross-compatibility across graphics cards.
     */
    public static void blurImage(int radius, NativeImage image) {
        if (radius <= 0) return;
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        NativeImage temp = new NativeImage(image.format(), width, height, false);
        
        // Run the separable tracks across the pass allocations
        for (int pass = 0; pass < radius; pass++) {
            // Pass 1: Blur Horizontally
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rSum = 0, gSum = 0, bSum = 0, aSum = 0;
                    int count = 0;
                    
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = x + dx;
                        if (nx >= 0 && nx < width) {
                            int pixel = image.getPixelRGBA(nx, y);
                            rSum += FastColor.ABGR32.red(pixel);
                            gSum += FastColor.ABGR32.green(pixel);
                            bSum += FastColor.ABGR32.blue(pixel);
                            aSum += FastColor.ABGR32.alpha(pixel);
                            count++;
                        }
                    }
                    temp.setPixelRGBA(x, y, FastColor.ABGR32.color(aSum / count, bSum / count, gSum / count, rSum / count));
                }
            }
            
            // Pass 2: Blur Vertically back into origin buffer matrix
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rSum = 0, gSum = 0, bSum = 0, aSum = 0;
                    int count = 0;
                    
                    for (int dy = -1; dy <= 1; dy++) {
                        int ny = y + dy;
                        if (ny >= 0 && ny < height) {
                            int pixel = temp.getPixelRGBA(x, ny);
                            rSum += FastColor.ABGR32.red(pixel);
                            gSum += FastColor.ABGR32.green(pixel);
                            bSum += FastColor.ABGR32.blue(pixel);
                            aSum += FastColor.ABGR32.alpha(pixel);
                            count++;
                        }
                    }
                    image.setPixelRGBA(x, y, FastColor.ABGR32.color(aSum / count, bSum / count, gSum / count, rSum / count));
                }
            }
        }
        
        temp.close();
    }
    
    public static String formatTime(double timeInSeconds) {
        int minutes = (int) (timeInSeconds / 60);
        double seconds = timeInSeconds % 60;
        return String.format("%02d:%06.3f", minutes, seconds);
    }
    
    public static boolean inBounds(int x, int y, int width, int height, int pointX, int pointY) {
        return pointX >= x && pointX <= (x + width) && pointY >= y && pointY <= (y + height);
    }
}