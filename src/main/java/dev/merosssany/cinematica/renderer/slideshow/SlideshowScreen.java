package dev.merosssany.cinematica.renderer.slideshow;

import com.mojang.blaze3d.platform.NativeImage;
import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.FileManager;
import dev.merosssany.cinematica.core.audio.AudioPlayer;
import dev.merosssany.cinematica.core.audio.AudioThread;
import dev.merosssany.cinematica.core.data.CinematicaCommandContext;
import dev.merosssany.cinematica.core.data.CinematicaCommandParser;
import dev.merosssany.cinematica.core.data.ClientCameraMemory;
import dev.merosssany.cinematica.core.data.rendering.TextureInfo;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSlide;
import dev.merosssany.cinematica.renderer.FadeState;
import dev.merosssany.cinematica.renderer.OverflowData;
import dev.merosssany.cinematica.renderer.Renderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static dev.merosssany.cinematica.core.Cinematica.*;
import static dev.merosssany.cinematica.renderer.Renderer.*;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class SlideshowScreen extends Screen {
    protected final int totalStage;
    protected final SlideshowSettings settings;
    protected final float fadeSpeed;
    protected final boolean skippable;
    protected final Path root;
    protected AtomicBoolean textSkipped = new AtomicBoolean();
    protected boolean shouldRender;
    
    private int stage;
    private double lastTime;
    private double timePassed = 0;
    private FadeState fadeState;
    private float fadeProgress;
    private AudioThread thread;
    private boolean init;
    private TextureInfo currentTexture;
    private String failed = "";
    private boolean typing;
    private OverflowData cache;
    private double timeOnFirstRender = 0;
    private final double timeOnConstructor;
    private boolean textureLoadingAttempted = false; // Prevents spamming file I/O on failure
    
    public SlideshowScreen(SlideshowSettings settings) {
        this(settings, FileManager.getCinematicaFolder());
    }
    
    public SlideshowScreen(SlideshowSettings settings, Path root) {
        super(Component.literal("Cinematica Slideshow"));
        this.skippable = settings.skippable();
        this.settings = settings;
        this.totalStage = settings.slides().length;
        this.root = root;
        lastTime = glfwGetTime();
        fadeState = FadeState.FADE_FROM_BLACK;
        thread = new AudioThread(new AudioPlayer(), () -> thread.shutdown());
        if (!settings.musicPath().isEmpty()) {
            try {
                thread.startStream(getAsset(settings.musicPath(), root));
            } catch (IOException e) {
                Cinematica.getLogger().error("Failed to load music", e);
            }
        }
        fadeSpeed = settings.fadeSpeed();
        timeOnConstructor = glfwGetTime();
    }
    
    @Override
    protected void init() {
        super.init();
        if (!init) {
            thread.start();
            stopSounds();
            reset();
            init = true;
        }
    }
    
    @Override
    public void render(GuiGraphics graphics, int mx, int my, float pTick) {
        stopSounds();
        shouldRender = !ClientCameraMemory.render;
        if (timeOnFirstRender == 0) timeOnFirstRender = glfwGetTime();
        
        if (shouldRender) graphics.fill(0, 0, this.width, this.height, 0xFF000000);
        
        double current = glfwGetTime();
        double delta = current - lastTime;
        lastTime = current;
        timePassed += delta;
        
        SlideshowSlide currentStage = settings.slides()[stage];
        String subtext = currentStage.subtext();
        String title = currentStage.title();
        
        if (fadeState == FadeState.NONE && timePassed >= currentStage.secondsToSwitch() && shouldRender) {
            fadeState = FadeState.FADE_TO_BLACK;
            fadeProgress = 0;
        }
        
        String locationName = "cinematica_slideshow_" + Cinematica.formalize(settings.name());
        
        if (currentTexture == null && failed.isEmpty() && !textureLoadingAttempted) {
            textureLoadingAttempted = true;
            try (InputStream stream = getAsset(currentStage.assetPath(), root)) {
                currentTexture = loadTexture(stream, locationName, stage);
            } catch (FileNotFoundException e) {
                failed = "File " + currentStage.assetPath() + " is not found.\nIt maybe a failed extraction.";
                Cinematica.getLogger().error(failed, e);
            } catch (IOException e) {
                failed = e.getMessage();
                Cinematica.getLogger().error(failed, e);
            }
        }
        
        if (currentTexture != null) {
            renderTexture(graphics, currentStage);
        }
        
        if (!subtext.isEmpty() || !title.isEmpty()) {
            renderText(graphics, subtext, title, currentStage);
        }
        
        renderVignette(graphics, currentStage);
        
        if (fadeState != FadeState.NONE) {
            fadeProgress += (float) (fadeSpeed * delta);
            if (fadeProgress > 1.0f) fadeProgress = 1.0f;
            
            float visualAlpha = fadeState == FadeState.FADE_FROM_BLACK ? 1.0f - fadeProgress : fadeProgress;
            int alphaBits = (int) (visualAlpha * 255.0f);
            int color = (alphaBits << 24) & 0xFF000000;
            
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 500);
            graphics.fill(0, 0, this.width, this.height, color);
            graphics.pose().popPose();
            
            if (fadeProgress >= 1.0f) {
                advance();
            }
        }
        
        if (failed != null && !failed.isEmpty()) {
            renderFailed(graphics, mx, my, failed);
        }
        
        if (Cinematica.debug) {
            double fadeDuration = 1.0 / settings.fadeSpeed();
            double totalStageDuration = currentStage.secondsToSwitch() + fadeDuration;
            double remainingTime = totalStageDuration - timePassed;
            
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 501);
            
            graphics.drawString(font, "Time before switch: " + formatTime(timePassed), 0, 0, 0xFFFFFFFF);
            graphics.drawString(font, "Total Stage Duration: " + formatTime(totalStageDuration), 0, font.lineHeight, 0xFFFFFFFF);
            graphics.drawString(font, "Remaining Time to switch: " + formatTime(Math.max(0, remainingTime)), 0, font.lineHeight * 2, 0xFFFFFFFF);
            graphics.drawString(font, "Time since created: " + formatTime(current - timeOnConstructor), 0, font.lineHeight * 3, 0xFFFFFFFF);
            graphics.drawString(font, "Total Time: " + formatTime(current - timeOnFirstRender), 0, font.lineHeight * 4, 0xFFFFFFFF);
            graphics.drawString(font, String.format("Fade progress: %.2f (%s)", fadeProgress, fadeState.name()), 0, font.lineHeight * 5, 0xFFFFFFFF);
            
            graphics.pose().popPose();
        }
    }
    
    protected void stopSounds() {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        if (settings.stopAudio()) {
            soundManager.stop();
        } else {
            soundManager.stop(null, SoundSource.MUSIC);
        }
    }
    
    public float getFadeProgress() {
        return fadeProgress;
    }
    
    protected void advance() {
        if (fadeState == FadeState.FADE_TO_BLACK) {
            if (stage < totalStage - 1) {
                stage++;
                reset();
            } else {
                end();
            }
        } else {
            fadeState = FadeState.NONE;
        }
    }
    
    public void nextStage() {
        if (!textSkipped.get()) {
            textSkipped.set(true);
            setTyping(false);
        }
        if (getFadeState() != FadeState.FADE_TO_BLACK) {
            setFadeState(FadeState.FADE_TO_BLACK);
            setFadeProgress(0);
        }
    }
    
    protected void reset() {
        timePassed = 0;
        fadeState = FadeState.FADE_FROM_BLACK;
        fadeProgress = 0;
        textureLoadingAttempted = false;
        
        if (currentTexture != null && currentTexture.texture() != null) {
            currentTexture.texture().close();
            Minecraft.getInstance().getTextureManager().release(currentTexture.location());
            currentTexture = null;
        }
        
        failed = "";
        ClientCameraMemory.render = false;
        cache = null;
        textSkipped.set(false);
        
        List<String> commands = settings.slides()[stage].commands();
        if (commands != null) {
            for (String cmd : commands) {
                String[] parts = cmd.split(" ");
                String[] params = new String[parts.length - 1];
                System.arraycopy(parts, 1, params, 0, params.length);
                CinematicaCommandContext context = new CinematicaCommandContext(params);
                CinematicaCommandParser.get().run(parts[0], context);
                if (context.failure() != null) {
                    Cinematica.getLogger().warn(context.failure());
                }
            }
        }
    }
    
    protected void renderFailed(GuiGraphics graphics, int mx, int my, String failed) {
        graphics.drawCenteredString(font, "⚠ Asset Error", this.width / 2, this.height / 2, 0xFFFF5555);
        List<Component> tooltipLines = Arrays.stream(failed.split("\n"))
                .map(Component::literal)
                .collect(Collectors.toList());
        graphics.renderComponentTooltip(font, tooltipLines, mx, my);
    }
    
    public void setFadeProgress(float fadeProgress) {
        this.fadeProgress = fadeProgress;
    }
    
    protected void end() {
        this.onClose();
    }
    
    protected void renderVignette(GuiGraphics graphics, SlideshowSlide currentStage) {
        if (currentStage.vignette() != null && currentStage.vignette().enable())
            Renderer.drawVignette(graphics, width, height, currentStage.vignette().color());
    }
    
    protected void renderText(GuiGraphics graphics, String subtext, String title, SlideshowSlide currentStage) {
        int width = (int) Math.max(font.width(subtext), font.width(title) * 1.5f) + 5;
        int height = font.lineHeight + 3;
        int speed = currentStage.typingSpeed();
        String visibleText = typewriter(subtext, speed);
        
        typing = !visibleText.equals(subtext);
        
        int posX;
        int posY = this.height - (settings.offset().y + (height * 2));
        
        if (settings.alternateTextPosition() && stage % 2 == 0)
            posX = settings.offset().x;
        else
            posX = this.width - settings.offset().x - width;
        
        int titleColor = currentStage.titleColor() != null && !currentStage.titleColor().isEmpty()? toHex(currentStage.titleColor()) : 0xFFFFFFFF;
        int textColor = currentStage.textColor() != null && !currentStage.textColor().isEmpty()? toHex(currentStage.textColor()) : 0xFFFFFFFF;
        int backgroundColor = currentStage.backgroundColor() != null && !currentStage.backgroundColor().isEmpty()? toHex(currentStage.backgroundColor()) : 0xFF000000;
        
        if (settings.largerTextBackground()) {
            if (cache == null) cache = Renderer.layoutText(font, List.of(subtext.split("\n")), this.width);
            
            float scale = 1.5f;
            int textHeight = cache.height() + 2;
            int titleHeight = (int) (font.lineHeight * scale);
            int gradientTopHeight = titleHeight + (titleHeight / 2);
            int gradientTotalHeight = textHeight + gradientTopHeight;
            int pYText = this.height - textHeight - 8;
            int pXText = this.width / 2;
            int pYTitle = gradientTopHeight + titleHeight;
            
            graphics.fillGradient(0, this.height - gradientTotalHeight - 8, this.width, pYText, 0x00000000, 0xFF000000);
            graphics.fill(0, pYText, this.width, this.height, 0xFF000000);
            
            drawScaledString(font, graphics, title, this.width / 2, pYTitle, scale, titleColor, true);
            
            for (int i = 0; i < cache.lines().size(); i++) {
                int nativeY = pYText + (i * (font.lineHeight + 1));
                FormattedCharSequence line = getFormattedCharSequence(cache, i, cache.lines().size(), speed);
                graphics.drawCenteredString(font, line, pXText, nativeY, textColor);
            }
        } else {
            graphics.fill(posX - 5, posY - 5, posX + width + 5, posY + (height * 2) + 5, backgroundColor);
            drawScaledString(this.font, graphics, currentStage.title(), posX + (width / 2), posY, 1.5f, titleColor, true);
            graphics.drawCenteredString(this.font, visibleText, posX + (width / 2), posY + height + 2, textColor);
        }
    }
    
    protected boolean isFailed() {
        return !failed.isEmpty();
    }
    
    protected FormattedCharSequence getFormattedCharSequence(OverflowData overflow, int i, int total, int speed) {
        if (textSkipped.get()) {
            if (total - 1 == i) setTyping(false);
            return (visitor) -> overflow.lines().get(i).accept(visitor);
        }
        
        int previousChars = 0;
        for (int j = 0; j < i; j++) {
            previousChars += overflow.lengths()[j];
        }
        
        int totalCharsToShow = (int) (getTimePassed() * speed);
        int charsForThisLine = Math.max(0, totalCharsToShow - previousChars);
        int maxChars = overflow.lengths()[i];
        int finalCharsToShow = Math.min(maxChars, charsForThisLine);
        
        if (finalCharsToShow < maxChars) {
            setTyping(true);
        } else if (total - 1 == i) {
            setTyping(false);
            textSkipped.set(true);
        }
        
        return (visitor) -> {
            int[] count = {0};
            return overflow.lines().get(i).accept((index, style, codePoint) -> {
                if (count[0] < finalCharsToShow) {
                    count[0]++;
                    return visitor.accept(index, style, codePoint);
                }
                return false;
            });
        };
    }
    
    protected static int toHex(String text) {
        return (int) Long.parseUnsignedLong(text.replace("#", ""), 16);
    }
    
    private String typewriter(String subtext, int speed) {
        int charsToShow = (int) Math.min(subtext.length(),
                Math.max(0, (timePassed - (1.0 / fadeSpeed)) * speed));
        return subtext.substring(0, charsToShow);
    }
    
    protected TextureInfo loadTexture(InputStream stream, String locationName, int stage) throws IOException {
        // FIX #2: Wrap NativeImage in try-with-resources to prevent massive off-heap VRAM leak
        try (NativeImage image = NativeImage.read(stream)) {
            int radius = settings.slides()[stage].blurRadius();
            blurImage(radius, image);
            
            // DynamicTexture copies pixel buffers into native bytes. Releasing the initial image is critical.
            DynamicTexture tex = new DynamicTexture(image);
            float imgW = image.getWidth();
            float imgH = image.getHeight();
            
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Cinematica.MODID, locationName + "_" + stage);
            Minecraft.getInstance().getTextureManager().register(location, tex);
            return new TextureInfo(imgW, imgH, tex, location);
        }
    }
    
    protected void renderTexture(GuiGraphics graphics, SlideshowSlide currentStage) {
        if (!shouldRender) return;
        
        float imgW = currentTexture.width();
        float imgH = currentTexture.height();
        
        float baseScale = Math.max((float) this.width / imgW, (float) this.height / imgH);
        
        double totalTextTime = (double) currentStage.subtext().length() / currentStage.typingSpeed();
        double animationDuration = totalTextTime + currentStage.secondsToSwitch() + fadeSpeed;
        
        float progress = (float) (timePassed / animationDuration);
        progress = Math.min(progress, 1.0f);
        float currentZoom = 1.0f;
        float panX = 0;
        float panY = 0;
        
        float eased = progress * progress * (3 - 2 * progress); // smoothstep
        
        if (currentStage.kenBurns() != null && currentStage.kenBurns().useKenBurns()) {
            float zoomStart = (stage % 2 == 0) ? 1.1f : 1.0f;
            float zoomEnd = (stage % 2 == 0) ? 1.0f : 1.1f;
            currentZoom = zoomStart + (zoomEnd - zoomStart) * progress;
            
            if (currentStage.kenBurns().panCameraHorizontally()) {
                float totalWidth = imgW * baseScale * currentZoom;
                float extraWidth = totalWidth - this.width;
                
                if (extraWidth > 0) {
                    if (stage % 2 == 0) {
                        panX = (extraWidth / 2f) - (eased * extraWidth);
                    } else {
                        panX = -(extraWidth / 2f) + (eased * extraWidth);
                    }
                }
            }
            
            if (currentStage.kenBurns().panCameraVertically()) {
                float extraHeight = (imgH * baseScale * currentZoom) - this.height;
                if (extraHeight > 0) {
                    panY = (stage % 2 == 0)
                            ? (extraHeight / 2f) - (eased * extraHeight)
                            : -(extraHeight / 2f) + (eased * extraHeight);
                }
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
        graphics.blit(currentTexture.location(), 0, 0, 0, 0, (int) imgW, (int) imgH, (int) imgW, (int) imgH);
        
        graphics.pose().popPose();
        
        if (currentStage.tint() != null) graphics.fill(0, 0, width, height, currentStage.tint().toHexadecimalARGB());
    }
    
    public int getStage() {
        return stage;
    }
    
    public void setStage(int stage) {
        this.stage = stage;
    }
    
    public FadeState getFadeState() {
        return fadeState;
    }
    
    protected void setFadeState(FadeState fadeState) {
        this.fadeState = fadeState;
    }
    
    protected double getTimePassed() {
        return timePassed;
    }
    
    protected void resetTime() {
        timePassed = 0;
        lastTime = glfwGetTime();
    }
    
    public boolean isTyping() {
        return typing;
    }
    
    protected void setTyping(boolean typing) {
        this.typing = typing;
    }
    
    protected void cleanup() {
        if (currentTexture != null) {
            currentTexture.texture().close();
            Minecraft.getInstance().getTextureManager().release(currentTexture.location());
            currentTexture = null;
        }
        
        thread.addTask(() -> {
            AudioPlayer player = thread.getPlayer();
            player.startFadeOut(1.5f);
            thread.requestExitAfterPlayback();
        });
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return skippable;
    }
    
    @Override
    public void onClose() {
        super.onClose();
        cleanup();
    }
    
    @Override
    public void removed() {
        super.removed();
        cleanup();
    }
    
    @Override
    public boolean keyPressed(int keyCode, int pScanCode, int pModifiers) {
        if (this.skippable) {
            nextStage();
            return true;
        }
        return super.keyPressed(keyCode, pScanCode, pModifiers);
    }
}