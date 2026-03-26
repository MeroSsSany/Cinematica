package dev.merosssany.cinematica.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.audio.AudioPlayer;
import dev.merosssany.cinematica.core.audio.AudioThread;
import dev.merosssany.cinematica.core.data.TextureInfo;
import dev.merosssany.cinematica.core.data.intro.SlideshowSettings;
import dev.merosssany.cinematica.core.data.intro.SlideshowSlide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SlideshowScreen extends Screen {
    private int stage;
    private final int totalStage;
    private final SlideshowSettings settings;
    private double lastTime;
    private double timePassed = 0;
    private FadeState fadeState;
    private float fadeProgress;
    private float fadeSpeed;
    private final TextureInfo[] textures;
    private final String[] failed;
    private AudioThread thread;
    private final boolean skippable;
    
    public SlideshowScreen(SlideshowSettings settings) {
        super(Component.literal("Cinematica Gallery"));
        this.skippable = settings.skippable();
        this.settings = settings;
        this.totalStage = settings.stages().length;
        lastTime = GLFW.glfwGetTime();
        String locationName = "cinematica_intro_" + settings.name();
        fadeState = FadeState.FADE_FROM_BLACK;
        thread = new AudioThread(new AudioPlayer(), () -> {
            thread.shutdown();
        });
        thread.startStream(settings.musicPath());
        
        textures = new TextureInfo[totalStage];
        failed = new String[textures.length];
        fadeSpeed = settings.fadeSpeed();
        
        for (int i = 0; i < totalStage; i++) {
            SlideshowSlide galleryStage = settings.stages()[i];
            
            if (galleryStage.isImage()) {
                try (FileInputStream stream = new FileInputStream(galleryStage.assetPath())) {
                    NativeImage image = NativeImage.read(stream);
                    DynamicTexture texture = new DynamicTexture(image);
                    
                    float imgW = image.getWidth();
                    float imgH = image.getHeight();
                    
                    ResourceLocation location = new ResourceLocation(Cinematica.MODID, locationName + "_" + i);
                    image.close();
                    
                    textures[i] = new TextureInfo(imgW, imgH, texture, location);
                    
                    Minecraft.getInstance().getTextureManager().register(location, texture);
                    
                    
                } catch (FileNotFoundException e) {
                    failed[i] = "File " + galleryStage.assetPath() + " is not found.\nIt maybe a failed extraction.";
                    e.printStackTrace();
                    
                } catch (IOException e) {
                    failed[i] = e.getMessage();
                    e.printStackTrace();
                }
            } else textures[i] = null;
        }
        
        thread.start();
    }
    
    @Override
    public void render(GuiGraphics graphics, int mx, int my, float pTick) {
        graphics.fill(0, 0, this.width, this.height, 0xFF000000);
        
        double current = GLFW.glfwGetTime();
        double delta = current - lastTime;
        lastTime = current;
        timePassed += delta;
        
        SlideshowSlide currentStage = settings.stages()[stage];
        String subtext = currentStage.subtext();
        String title = currentStage.title();
        
        if (fadeState == FadeState.NONE && timePassed >= settings.secondsToSwitch()) {
            fadeState = FadeState.FADE_TO_BLACK;
            fadeProgress = 0;
        }
        
        TextureInfo texture = textures[stage];
        if (texture != null) {
            float imgW = texture.width();
            float imgH = texture.height();
            
            // Calculate Base Scale to fill the screen
            float baseScale = Math.max((float) this.width / imgW, (float) this.height / imgH);
            
            // Progress and Animation Toggles
            float progress = (float) (timePassed / settings.secondsToSwitch());
            progress = Math.min(progress, 1.0f);
            float currentZoom = 1.0f;
            float panX = 0;
            float panY = 0;
            
            float eased = progress * progress * (3 - 2 * progress); // smoothstep
            
            if (settings.kenBurns()) {
                // --- ZOOM LOGIC ---
                // Alternate Zoom In / Zoom Out
                float zoomStart = (stage % 2 == 0) ? 1.1f : 1.0f;
                float zoomEnd = (stage % 2 == 0) ? 1.0f : 1.1f;
                currentZoom = zoomStart + (zoomEnd - zoomStart) * progress;
                
                // --- PAN LOGIC ---
                // Calculate the "extra" space we have to move within
                // We multiply by currentZoom because a zoomed-in image has more "slack" to pan
                float totalWidth = imgW * baseScale * currentZoom;
                float extraWidth = totalWidth - this.width;
                
                // Pan horizontally based on stage index
                if (extraWidth > 0) {
                    if (stage % 2 == 0) {
                        // Pan Right to Left
                        panX = (extraWidth / 2f) - (eased * extraWidth);
                    } else {
                        // Pan Left to Right
                        panX = -(extraWidth / 2f) + (eased * extraWidth);
                    }
                }
                
                float extraHeight = (imgH * baseScale * currentZoom) - this.height;
                
                if (extraHeight > 0) {
                    panY = (stage % 2 == 0)
                            ? (extraHeight / 2f) - (eased * extraHeight)
                            : -(extraHeight / 2f) + (eased * extraHeight);
                }
            }
            
            graphics.pose().pushPose();

            float pivotX = this.width * currentStage.anchor().x;
            float pivotY = this.height * currentStage.anchor().y;
            graphics.pose().translate(pivotX, pivotY, 0);
            
            graphics.pose().translate(panX, panY, 0);

            float finalScale = baseScale * currentZoom;
            graphics.pose().scale(finalScale, finalScale, 1.0f);

            graphics.pose().translate(-imgW * currentStage.anchor().x, -imgH * currentStage.anchor().y, 0);

            graphics.blit(texture.location(), 0, 0, 0, 0, (int)imgW, (int)imgH, (int)imgW, (int)imgH);
            
            graphics.pose().popPose();
        }
        
        if (!subtext.isEmpty() || !title.isEmpty()) {
            int width = Math.max(font.width(subtext), font.width(title)) + 5;
            int height = font.lineHeight + 3;
            int charsToShow = (int) Math.min(currentStage.subtext().length(),
                    Math.max(0, (timePassed - (1.0 / fadeSpeed)) * 10)); // 10 chars per second
            String visibleText = currentStage.subtext().substring(0, charsToShow);
            
            int posX;
            int posY = this.height - (settings.offset().y + (height * 2));
            
            if (settings.alternateTextPosition() && stage % 2 == 0)
                posX = settings.offset().x; // Left side
            else
                posX = this.width - settings.offset().x - width; // Right side
            
            if (settings.bottomView()) {
                // 1. Calculate the Y-coordinates for better readability
                int gradientTop = this.height - settings.offset().y;
                int solidTop = this.height - (settings.offset().y / 2);
                int centerX = this.width / 2;
                
                // 2. Background: Smooth fade into solid black at the bottom
                graphics.fillGradient(0, gradientTop, this.width, solidTop, 0x00000000, 0xFF000000);
                graphics.fill(0, solidTop, this.width, this.height, 0xFF000000);
                
                // 3. Title: Scaled and centered
                // We pass centerX and set 'center' to true for the helper method
                drawScaledString(graphics, title, centerX, solidTop - font.lineHeight - 5, 1.5f, 0xFFC8C8C8, true);
                
                // 4. Subtext: Centered in the solid black bar area
                // We use drawCenteredString to ensure it's perfectly balanced
                int subtextY = solidTop + ( (this.height - solidTop) / 2 ) - (font.lineHeight / 2);
                graphics.drawCenteredString(font, visibleText, centerX, subtextY, 0xFFFFFFFF);
                
            } else {
                // Floating Box Logic (Keep as is, but ensuring the scale/center math is consistent)
                graphics.fill(posX - 5, posY - 5, posX + width + 5, posY + (height * 2) + 5, 0xCC000000);
                drawScaledString(graphics, currentStage.title(), posX + (width / 2), posY, 1.5f, 0xFFC8C8C8, true);
                graphics.drawCenteredString(this.font, visibleText, posX + (width / 2), posY + height + 2, 0xFFFFFFFF);
            }
        }
        
        if (fadeState != FadeState.NONE) {
            fadeProgress += (float) (fadeSpeed * delta);
            
            if (fadeProgress > 1.0f) fadeProgress = 1.0f;
            
            float visualAlpha = fadeState == FadeState.FADE_FROM_BLACK? 1.0f - fadeProgress : fadeProgress;
            
            int alphaBits = (int) (visualAlpha * 255.0f);
            int color = (alphaBits << 24) & 0xFF000000; // Force black RGB
            
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 500);
            graphics.fill(0, 0, this.width, this.height, color);
            graphics.pose().popPose();
            
            // Transition Logic
            if (fadeProgress >= 1.0f) {
                if (fadeState == FadeState.FADE_TO_BLACK) {
                    if (stage < totalStage - 1) {
                        stage++;
                        timePassed = 0; // Reset typing effect for next stage
                        fadeState = FadeState.FADE_FROM_BLACK;
                        fadeProgress = 0;
                    } else {
                        this.onClose();
                    }
                } else {
                    fadeState = FadeState.NONE;
                }
            }
        }
        
        if (failed[stage] != null && !failed[stage].isEmpty()) {
            graphics.drawCenteredString(font, "⚠ Asset Error", this.width / 2, this.height / 2, 0xFFFF5555);
            
            List<Component> tooltipLines = Arrays.stream(failed[stage].split("\n"))
                    .map(Component::literal)
                    .collect(Collectors.toList());
            
            graphics.renderComponentTooltip(font, tooltipLines, mx, my);
        }
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return skippable;
    }
    
    @Override
    public void onClose() {
        super.onClose();
        for (TextureInfo tex : textures) {
            if (tex != null && tex.texture() != null) {
                tex.texture().close();
                Minecraft.getInstance().getTextureManager().release(tex.location());
            }
        }
        thread.addTask(() -> {
            AudioPlayer player = thread.getPlayer();
            player.startFadeOut(1.5f);
            thread.requestExitAfterPlayback();
        });
    }
    
    public float getFadeSpeed() {
        return fadeSpeed;
    }
    
    public void setFadeSpeed(float fadeSpeed) {
        this.fadeSpeed = fadeSpeed;
    }
    
    protected void drawScaledString(GuiGraphics graphics, String text, int x, int y, float scale, int color, boolean center) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        
        if (center) {
            graphics.drawCenteredString(this.font, text, 0, 0, color);
        } else {
            graphics.drawString(this.font, text, 0, 0, color, true);
        }
        
        graphics.pose().popPose();
    }
}
