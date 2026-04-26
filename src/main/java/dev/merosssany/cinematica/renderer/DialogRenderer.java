package dev.merosssany.cinematica.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.dialog.DialogSettings;
import dev.merosssany.cinematica.core.data.dialog.DialogStage;
import dev.merosssany.cinematica.core.data.rendering.TextureInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DialogRenderer {
    public static class Cache {
        private final DialogSettings settings;
        private List<FormattedCharSequence> parsedDialog;
        private int currentDialog;
        private int height;
        private int currentTypingLine;
        private int selected;
        private double timePassed;
        private boolean typed;
        private boolean options;
        private TextureInfo tempTex;
        private String input;
        
        public Cache(DialogSettings settings) {
            this.settings = settings;
        }
        
        public boolean isOverlaySupported() {
            return !options;
        }
    }
    
    public static boolean render(
            GuiGraphics graphics,
            Font font,
            Cache cache,
            int screenWidth,
            int screenHeight,
            double delta
    ) throws IOException {
        cache.timePassed += delta;
        List<FormattedCharSequence> parsedDialog = cache.parsedDialog;
        DialogSettings settings = cache.settings;
        DialogStage current = settings.dialogStages()[cache.currentDialog];
        
        if (cache.parsedDialog == null) {
            List<String> text = Arrays.stream(settings.dialogStages()[cache.currentDialog].text().split("\n")).toList();
            
            // Check for overflow
            checkOverflow(cache, font, text, screenWidth);
        }
        
        int minY = screenHeight - cache.height - 64;
        
        if (current.useTextInput() || current.options().length > 0) {
            cache.options = true;
            if (current.useTextInput()) minY -= font.lineHeight + 8;
            else minY -= 40;
        }
        
        int minTextY = minY + 64;
        
        graphics.fill(16, minY, screenWidth - 16, screenHeight, 0xFF000000);
        graphics.fillGradient(16, minY - 16, screenWidth - 16, minY, 0x00000000, 0xFF000000);
        
        if (current.useTexture()) {
            
            if (cache.tempTex == null) {
                String texture = current.texture();
                
                if (texture != null && !texture.isEmpty()) {
                    File textureFile = settings.root().resolve(texture).toFile();
                    
                    if (textureFile.exists()) {
                        try (FileInputStream stream = new FileInputStream(textureFile)) {
                            NativeImage image = NativeImage.read(stream);
                            DynamicTexture dynamicTexture = new DynamicTexture(image);
                            ResourceLocation location = new ResourceLocation(Cinematica.MODID, "cinematica_dialog_"+settings.dialogName()+"_"+cache.currentDialog);
                            Minecraft.getInstance().getTextureManager().register(location, dynamicTexture);
                            
                            cache.tempTex = new TextureInfo(
                                    image.getWidth(),
                                    image.getHeight(),
                                    dynamicTexture,
                                    location
                            );
                            
                            image.close();
                        }
                    } else {
                        ResourceLocation location = new ResourceLocation(texture);
                        
                        if (Minecraft.getInstance().getResourceManager().getResource(location).isPresent()) {
                            cache.tempTex = TextureInfo.fromResourceLocation(location);
                        }
                    }
                } else cache.tempTex = TextureInfo.empty();
            }
            
            if (!cache.tempTex.isEmpty()) {
                int texX = 24;
                int texY = minY + 16;
                int texSize = 64;
                
                graphics.fill(texX - 2, texY - 2, texX + texSize + 2, texY + texSize + 2, 0xFFFFFFFF);
                graphics.blit(cache.tempTex.location(), texX, texY, 0, 0, texSize, texSize, texSize, texSize);
            }
        }
        
        String name = "The World";
        if (current.name() != null && !current.name().isEmpty()) {
            name = current.name();
        } else if (current.entityType() != null && !current.entityType().isEmpty()) {
            ResourceLocation location = new ResourceLocation(current.entityType());
            
            Optional<EntityType<?>> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(location);
            
            if (entityType.isPresent()) {
                name = entityType.get().getDescription().getString();
            }
        }
        
        graphics.drawString(font, name, 16, minY + (font.lineHeight / 2), 0xFFFFFFFF);
        
        for (int i = 0; i < parsedDialog.size(); i++) {
            var dialog = parsedDialog.get(i);
            int pY = minTextY + (font.lineHeight * i);
            int pX = 16;
            
            if (current.useTexture() && !cache.tempTex.isEmpty()) pX = 100;
            
            if (i < cache.currentTypingLine || cache.typed) {
                graphics.drawString(font, dialog, pX, pY, current.color());
                
            } else if (i == cache.currentTypingLine) {
                int maxChars = getLength(dialog);
                int charsToShow = (int) Math.min(
                        maxChars,
                        cache.timePassed * 10
                );
                
                FormattedCharSequence line = (visitor) -> {
                    int[] count = {0};
                    return dialog.accept((index, style, codePoint) -> {
                        if (count[0] < charsToShow) {
                            count[0]++;
                            return visitor.accept(index, style, codePoint);
                        }
                        return false;
                    });
                };
                
                if (maxChars == charsToShow) {
                    cache.currentTypingLine++;
                    cache.timePassed = 0;
                    
                    if (cache.currentTypingLine >= parsedDialog.size()) {
                        cache.typed = true;
                    }
                }
                
                graphics.drawString(font, line, pX, pY, current.color());
            }
        }
        
        if (cache.options) {
            if (current.useTextInput()) {
            
            }
        }
        
        if (cache.typed) {
            if (cache.timePassed >= 2.5 && !cache.options) {
                // Cleanup VRAM
                if (cache.tempTex != null && !cache.tempTex.isEmpty()) {
                    if (cache.tempTex.texture() != null) cache.tempTex.texture().close();
                    Minecraft.getInstance().getTextureManager().release(cache.tempTex.location());
                }
                
                // Reset for next stage
                cache.typed = false;
                cache.options = false;
                cache.parsedDialog = null;
                cache.tempTex = null;
                cache.currentTypingLine = 0;
                cache.currentDialog++;
                cache.timePassed = 0;
                
                return cache.currentDialog >= settings.dialogStages().length;
            }
        }
        
        return false;
    }
    
    private static int getLength(FormattedCharSequence charSequence) {
        int[] l = {0};
        charSequence.accept((index, style, codepoint) -> {
            l[0]++;
            return true;
        });
        return l[0];
    }
    
    private static void checkOverflow(Cache cache, Font font, List<String> text, int screenWidth) {
        List<FormattedCharSequence> wrappedLines = new ArrayList<>();
        int maxWidth = screenWidth - 32;
        int fontHeight = font.lineHeight + 1;
        int totalHeight;
        
        for (String originalLine : text) {
            List<FormattedCharSequence> splitLines = font.split(Component.literal(originalLine), maxWidth);
            
            wrappedLines.addAll(splitLines);
        }
        
        totalHeight = wrappedLines.size() * fontHeight;
        
        cache.parsedDialog = wrappedLines;
        cache.height = totalHeight;
    }
}
