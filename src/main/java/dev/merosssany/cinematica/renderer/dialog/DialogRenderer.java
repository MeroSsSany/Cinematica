package dev.merosssany.cinematica.renderer.dialog;

import com.mojang.blaze3d.platform.NativeImage;
import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.dialog.DialogSettings;
import dev.merosssany.cinematica.core.data.dialog.DialogStage;
import dev.merosssany.cinematica.core.data.rendering.TextureInfo;
import dev.merosssany.cinematica.renderer.OverflowData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static dev.merosssany.cinematica.renderer.Renderer.layoutText;

public class DialogRenderer {
    public static class Cache {
        private final DialogSettings settings;
        private final Path root;
        
        private List<FormattedCharSequence> parsedDialog;
        private int currentDialog;
        private int height;
        private int currentTypingLine;
        private int selected;
        private double timePassed;
        private boolean typed;
        private boolean options;
        private TextureInfo tempTex;
        private EditBox input;
        private int[] lineLengths;
        
        public Cache(DialogSettings settings, Path root) {
            this.settings = settings;
            this.root = root;
        }
        
        public boolean isOverlaySupported() {
            return !options;
        }
        
        public boolean consumeClick(int mouseX, int mouseY, int button) {
            if (input == null) return false;
            
            int minX = input.getX();
            int maxX = input.getX() + input.getWidth();
            int minY = input.getY();
            int maxY = input.getY() + input.getHeight();
            
            boolean pressed = mouseX <= maxX && mouseX >= minX
                    && mouseY <= maxY && mouseY >= minY;
            
            if (pressed) input.mouseClicked(mouseX, mouseY, button);
            input.setFocused(pressed);
            
            return pressed;
        }
        
        public void charTyped(char codePoint, int modifiers) {
            if (input != null && input.isFocused()) input.charTyped(codePoint, modifiers);
            else typed = true;
        }
        
        public void keyPressed(int keyCode, int scanCode, int modifiers) {
            if (input != null && input.isFocused()) input.keyPressed(keyCode, scanCode, modifiers);
        }
        
        public void apply(OverflowData data) {
            lineLengths = data.lengths();
            parsedDialog = data.lines();
            height = data.height();
        }
    }
    
    public static boolean render(
            GuiGraphics graphics,
            Font font,
            Cache cache,
            int screenWidth,
            int screenHeight,
            int mouseX,
            int mouseY,
            float pTick,
            double delta
    ) {
        cache.timePassed += delta;
        DialogSettings settings = cache.settings;
        DialogStage current = settings.dialogStages()[cache.currentDialog];
        
        if (cache.parsedDialog == null) {
            List<String> text = Arrays.stream(settings.dialogStages()[cache.currentDialog].text().split("\n")).toList();
            cache.apply(layoutText(font, text, screenWidth));
        }
        
        List<FormattedCharSequence> parsedDialog = cache.parsedDialog;
        
        int minY = screenHeight - cache.height - 64;
        
        if (current.textInput().enabled() || current.options().length > 0) {
            cache.options = true;
            if (current.textInput().enabled()) minY -= font.lineHeight + 8;
            else minY -= 40;
        }
        
        int minTextY = minY + 64;
        
        graphics.fill(16, minY, screenWidth - 16, screenHeight, 0xFF000000);
        graphics.fillGradient(16, minY - 16, screenWidth - 16, minY, 0x00000000, 0xFF000000);
        
        if (current.useTexture()) {
            if (cache.tempTex == null) {
                String texture = current.texture();
                
                if (texture != null && !texture.isEmpty()) {
                    File textureFile = cache.root.resolve(texture).toFile();
                    
                    if (textureFile.exists()) {
                        try (FileInputStream stream = new FileInputStream(textureFile)) {
                            NativeImage image = NativeImage.read(stream);
                            DynamicTexture dynamicTexture = new DynamicTexture(image);
                            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Cinematica.modId, "cinematica_dialog_" + settings.dialogName() + "_" + cache.currentDialog);
                            Minecraft.getInstance().getTextureManager().register(location, dynamicTexture);
                            
                            cache.tempTex = new TextureInfo(
                                    image.getWidth(),
                                    image.getHeight(),
                                    dynamicTexture,
                                    location
                            );
                            
                            image.close();
                            
                        } catch (Exception e) {
                            Cinematica.getLogger().error("Failed to load texture {}", textureFile.getAbsolutePath(), e);
                            cache.tempTex = TextureInfo.empty();
                        }
                        
                    } else {
                        ResourceLocation location = ResourceLocation.parse(texture);
                        
                        if (Minecraft.getInstance().getResourceManager().getResource(location).isPresent()) {
                            cache.tempTex = TextureInfo.fromResourceLocation(location);
                        }
                    }
                    
                } else cache.tempTex = TextureInfo.empty();
            }
            
            if (cache.tempTex != null) {
                if (!cache.tempTex.isEmpty()) {
                    int texX = 24;
                    int texY = minY + 16;
                    int texSize = 64;
                    
                    graphics.fill(texX - 2, texY - 2, texX + texSize + 2, texY + texSize + 2, 0xFFFFFFFF);
                    graphics.blit(cache.tempTex.location(), texX, texY, 0, 0, texSize, texSize, texSize, texSize);
                }
            }
        }
        
        String name = "The World";
        if (current.name() != null && !current.name().isEmpty()) {
            name = current.name();
        } else if (current.entityType() != null && !current.entityType().isEmpty()) {
            ResourceLocation location = ResourceLocation.parse(current.entityType());
            
            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(location);
            
            if (entityType != null) {
                name = entityType.getDescription().getString();
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
                int maxChars = cache.lineLengths[i];
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
            if (current.textInput().enabled()) {
                if (cache.input == null) {
                    int inputY = minY + cache.height + 8;
                    
                    cache.input = new EditBox(
                            font,
                            16,
                            inputY,
                            screenWidth - 32,
                            font.lineHeight + 8,
                            Component.literal(current.textInput().ask())
                    );
                }
                
                EditBox box = cache.input;
                
                box.renderWidget(graphics, mouseX, mouseY, pTick);
            }
        }
        
        if (cache.typed) {
            if (cache.timePassed >= 2.5 && !cache.options) {
                // Cleanup VRAM
                if (cache.tempTex != null && !cache.tempTex.isEmpty()) {
                    if (cache.tempTex.texture() != null) cache.tempTex.texture().close();
                    Minecraft.getInstance().getTextureManager().release(cache.tempTex.location());
                    cache.tempTex = null;
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
}
