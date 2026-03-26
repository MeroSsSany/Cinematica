package dev.merosssany.cinematica.renderer;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.audio.AudioPlayer;
import dev.merosssany.cinematica.core.audio.AudioThread;
import dev.merosssany.cinematica.core.data.TextureInfo;
import dev.merosssany.cinematica.data.ResourceLocationReader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;

public class ScrollingTextScreen extends Screen {
    protected TextureInfo texture;
    protected String[] lines;
    protected double scrollFactor; // Precision is key for smooth movement
    protected float scrollSpeed; // Pixels per second
    protected AudioThread audioThread;
    protected boolean wave;
    protected final String finalMessage;
    protected float scale;
    protected float fadeSpeed;
    
    private boolean init;
    private boolean killed;
    private double lastFrame;
    private FadeState fadeState;
    private float fadeProgress;
    private double timePassed;
    private boolean renderFinal;
    private boolean scrollFinished = false;
    
    public ScrollingTextScreen(boolean wave, float speed, String text, String logo, String music, String finalMessage, float scale, float fadeSpeed) throws IOException {
        super(Component.literal("Cinematica Scrolling Text"));
        
        this.scrollSpeed = speed;
        this.wave = wave;
        this.finalMessage = finalMessage;
        this.scale = scale;
        this.fadeSpeed = fadeSpeed;
        
        if (logo == null || logo.isEmpty()) {
            texture = TextureInfo.fromResourceLocation(new ResourceLocation(Cinematica.MODID, "textures/gui/backgrounds/main_bg.png"));
        } else {
            texture = CinematicaImageReader.read(logo, "cinematica_scrolling_text_logo");
        }
        
        this.lines = text.split("\n");
        lastFrame = GLFW.glfwGetTime();
        scrollFactor = -height;
        
        audioThread = new AudioThread(new AudioPlayer(), () -> audioThread.shutdown());
        
        if (music != null && !music.isEmpty()) {
            File file = new File(music);
            
            if (file.exists()) audioThread.startStream(file);
            else audioThread.startStream(ResourceLocationReader.resourceLocationToStream(new ResourceLocation(music)));
        
            audioThread.start();
        }
        
        init = true;
    }
    
    @Override
    protected void init() {
        if (!init) return;
        this.scrollFactor = this.height + 20;
        this.lastFrame = GLFW.glfwGetTime();
        init = false;
    }
    
    @Override
    public void render(GuiGraphics graphics, int mx, int my, float pDelta) {
        double current = GLFW.glfwGetTime();
        double delta = current - lastFrame;
        lastFrame = current;
        
        // Update the scroll position
        scrollFactor -= (scrollSpeed * delta);
        
        // 1. Draw solid background
        graphics.fill(0, 0, width, height, 0xFF000000);
        
        // 2. Prepare transformations for the scroll
        graphics.pose().pushPose();
        
        // Move the entire "paper" by the scrollFactor (using floats for sub-pixel smoothness)
        graphics.pose().translate(0, (float) scrollFactor, 0);
        
        // --- Logo Rendering ---
        float logoScale = Math.min((float) width / texture.width(), (float) height / texture.height()) * 0.5f;
        int renderW = (int) (texture.width() * logoScale);
        int renderH = (int) (texture.height() * logoScale);
        int logoX = (width / 2) - (renderW / 2);
        
        graphics.blit(texture.location(), logoX, 0, 0, 0, renderW, renderH, renderW, renderH);
        
        // --- Text Rendering ---
        int textStartY = renderH + 20; // Start text below the logo
        int lineHeight = font.lineHeight + 4;
        
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().isEmpty()) continue;
            
            // Apply the wave effect per-line
            float wave;
            if (this.wave) wave = (float) Math.sin(current * 2.0 + i * 0.5) * 3.0f;
            else wave = 0;
            
            graphics.pose().pushPose();
            graphics.pose().translate(0, wave, 0); // Apply sub-pixel vertical wave
            
            graphics.drawCenteredString(font, lines[i], width / 2, textStartY + (i * lineHeight), 0xFFFFFFFF);
            
            graphics.pose().popPose();
        }
        
        graphics.pose().popPose();
        
        int totalScrollHeight = renderH + 20 + (lines.length * lineHeight);
        if (!scrollFinished && (scrollFactor + totalScrollHeight < -50)) {
            scrollFinished = true; // Only trigger this ONCE
            fadeState = FadeState.FADE_TO_BLACK;
            fadeProgress = 0;
        }
        
        if (renderFinal) {
            // 1. Calculate the alpha for the text based on the current fade
            // If we are FADING_FROM_BLACK, the text should be getting brighter
            float textAlpha = 1.0f;
            if (fadeState == FadeState.FADE_FROM_BLACK) {
                textAlpha = fadeProgress; // 0.0 at start of fade, 1.0 at end
            }
            
            
            int alphaBits = (int) (textAlpha * 255.0f);
            int textColor = (alphaBits << 24) | 0xFFFFFF; // ARGB: Alpha + White
            
            // 2. Draw the message with the calculated alpha
            float finalScale = 2.5f;
            int finalY = (this.height / 2) - (int)((font.lineHeight * finalScale) / 2);
            
            drawScaledString(graphics, finalMessage, this.width / 2, finalY, finalScale, textColor, true);
            
            // 3. Only start the "Exit Timer" once the fade is completely finished
            if (fadeState == FadeState.NONE) {
                timePassed += delta;
                if (timePassed > 5.0) {
                    this.onClose();
                }
            }
        }
        
        if (fadeState != FadeState.NONE) {
            fadeProgress += (float) (fadeSpeed * delta);
            
            if (fadeProgress > 1.0f) fadeProgress = 1.0f;
            
            // Transition Logic
            if (fadeProgress >= 1.0f) {
                if (fadeState == FadeState.FADE_TO_BLACK) {
                    fadeState = FadeState.FADE_FROM_BLACK;
                    renderFinal = true;
                    
                } else {
                    fadeState = FadeState.NONE;
                }
            }
        }
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
    
    @Override
    public void onClose() {
        super.onClose();
        cleanup();
    }
    
    public void cleanup() {
        if (killed) return;
        DynamicTexture texture = this.texture.texture();
        if (texture != null) texture.close();
        audioThread.addTask(()-> {
            audioThread.getPlayer().startFadeOut(0.5f);
            audioThread.requestExitAfterPlayback();
        });
        killed = true;
    }
    
    @Override
    public void removed() {
        super.removed();
        cleanup();
    }
}