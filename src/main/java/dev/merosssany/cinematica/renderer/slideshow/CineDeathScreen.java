package dev.merosssany.cinematica.renderer.slideshow;

import com.mojang.blaze3d.platform.NativeImage;
import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.RGBA;
import dev.merosssany.cinematica.core.data.death.DeathScreenContext;
import dev.merosssany.cinematica.core.data.rendering.TextureInfo;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSlide;
import dev.merosssany.cinematica.renderer.FadeState;
import dev.merosssany.cinematica.renderer.OverflowData;
import dev.merosssany.cinematica.renderer.Renderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static dev.merosssany.cinematica.renderer.Renderer.blurImage;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class CineDeathScreen extends SlideshowScreen {
    private double lastFrame;
    private double timePassed;
    private final List<Button> exitButtons = new ArrayList<>();
    private boolean started;
    private int prevStage = -1;
    private String cache;
    private boolean textSkipped;
    private boolean ended;
    
    protected final DeathScreenContext context;
    
    public CineDeathScreen(DeathScreenContext context) {
        super(context.settings());
        this.context = context;
        
        setFadeState(FadeState.FADE_FROM_BLACK);
        lastFrame = glfwGetTime();
    }
    
    @Override
    public void render(GuiGraphics graphics, int mx, int my, float pTick) {
        double current = glfwGetTime();
        timePassed += current - lastFrame;
        lastFrame = current;
        
        // Initial Delay logic
        if (timePassed >= 2.5 && !started) {
            resetTime();
            started = true;
        }
        
        if (started && !ended) {
            super.render(graphics, mx, my, pTick);
        } else {
            graphics.fill(0, 0, width, height, 0xFF000000);
        }
        
        if (ended) {
            int x = (int) (width * 0.5f);
            float y = height * 0.25f;
            Renderer.drawScaledString(font, graphics,"You died!", x, (int) y, 2.5f,0xFFFF0000, true);
            graphics.drawCenteredString(font, settings.name(), x, (int) (height * 3f), 0xFFFFFFFF);
            
            for (Renderable renderable : this.renderables) {
                renderable.render(graphics, mx, my, pTick);
            }
        }
        
        if (timePassed < 0.2) Renderer.drawVignette(graphics, width, height, RGBA.fromRGBA(168, 20, 25, 1));
    }
    
    @Override
    protected void renderText(GuiGraphics graphics, String subtext, String title, SlideshowSlide currentStage) {
        OverflowData overflow = Renderer.layoutText(this.font, getStrings(subtext), this.width);
        
        int pY = (this.height / 2) - (overflow.height() / 2);
        int pX = this.width / 2;
        int speed = currentStage.typingSpeed();
        
        for (int i = 0; i < overflow.lines().size(); i++) {
            FormattedCharSequence line = getFormattedCharSequence(overflow, i, overflow.lines().size(), speed);
            
            graphics.drawCenteredString(font, line, pX, pY + (i * (font.lineHeight + 1)), toHex(currentStage.textColor()));
        }
    }
    
    private @NotNull List<String> getStrings(String subtext) {
        if (getStage() != prevStage) {
            prevStage = getStage();
            LocalPlayer player = getMinecraft().player;
            
            // Safety checks for attacker data
            String attackerName = "The World";
            String attackerHealth = "0";
            if (context.entity() != null) {
                attackerName = context.entity().getDisplayName().getString();
                if (context.entity() instanceof LivingEntity living) {
                    attackerHealth = String.format("%.1f", living.getHealth());
                }
            }
            
            ResourceLocation dimLocation = player.level().dimension().location();
            String dimKey = "dimension." + dimLocation.getNamespace() + "." + dimLocation.getPath();
            String dimensionName = Component.translatable(dimKey).getString();
            
            if (dimensionName.equals(dimKey)) {
                dimensionName = StringUtils.capitalize(dimLocation.getPath());
            }
            
            cache = subtext
                    .replace("$player", player.getDisplayName().getString())
                    .replace("$attacker_health", attackerHealth)
                    .replace("$attacker", attackerName)
                    .replace("$fps", String.valueOf(getMinecraft().getFps()))
                    .replace("$x", String.format("%.1f", player.getX()))
                    .replace("$y", String.format("%.1f", player.getY()))
                    .replace("$z", String.format("%.1f", player.getZ()))
                    .replace("$health", String.format("%.1f", player.getHealth()))
                    .replace("$death_message", context.deathMessage())
                    .replace("$dimension", dimensionName)
            ;
        }
        return List.of(cache.split("\n"));
    }
    
    private @NotNull FormattedCharSequence getFormattedCharSequence(OverflowData overflow, int i, int total, int speed) {
        // Calculate how many characters were in previous lines to create a delay
        int previousChars = 0;
        for (int j = 0; j < i; j++) {
            previousChars += overflow.lengths()[j];
        }
        
        int totalCharsToShow = (int) (getTimePassed() * speed);
        int charsForThisLine = Math.max(0, totalCharsToShow - previousChars);
        int maxChars = overflow.lengths()[i];
        
        int finalCharsToShow = Math.min(maxChars, charsForThisLine);
        if (total - 1 == i) {
            setTyping(finalCharsToShow != maxChars);
            textSkipped = isTyping();
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
    
    @Override
    protected void init() {
        super.init();
        this.exitButtons.clear();
        
        // Create the buttons but keep them hidden/inactive initially
        Component respawnText = isHardcore()? Component.translatable("deathScreen.spectate") : Component.translatable("deathScreen.respawn");
        
        Button respawnBtn = Button.builder(respawnText, (btn) -> {
            this.minecraft.player.respawn();
            btn.active = false;
            onClose();
            getMinecraft().setScreen(null);
            
        }).bounds(this.width / 2 - 100, this.height / 2 + 20, 200, 20).build();
        
        Button titleBtn = Button.builder(Component.translatable("deathScreen.titleScreen"), (btn) -> {
            this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, this::handleExitToTitleScreen, true);
        }).bounds(this.width / 2 - 100, this.height / 2 + 45, 200, 20).build();
        
        this.exitButtons.add(this.addRenderableWidget(respawnBtn));
        this.exitButtons.add(this.addRenderableWidget(titleBtn));
        
        setButtonsVisible(ended);
    }
    
    private void setButtonsVisible(boolean visible) {
        for (Button btn : exitButtons) {
            btn.visible = visible;
            btn.active = visible;
        }
    }
    
    @Override
    protected void end() {
        Cinematica.getLogger().info("ended");
        ended = true;
        setButtonsVisible(true);
        cleanup();
    }
    
    private void handleExitToTitleScreen() {
        if (isHardcore()) {
            this.exitToTitleScreen();
        } else {
            ConfirmScreen confirm = new DeathScreen.TitleConfirmScreen((confirmed) -> {
                if (confirmed) this.exitToTitleScreen();
                else {
                    this.minecraft.player.respawn();
                    this.minecraft.setScreen(null);
                }
            }, Component.translatable("deathScreen.quit.confirm"), CommonComponents.EMPTY,
                    Component.translatable("deathScreen.titleScreen"), Component.translatable("deathScreen.respawn"));
            this.minecraft.setScreen(confirm);
        }
    }
    
    private boolean isHardcore() {
        return this.minecraft.level.getLevelData().isHardcore();
    }
    
    private void exitToTitleScreen() {
        if (this.minecraft.level != null) this.minecraft.level.disconnect();
        this.minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
        this.minecraft.setScreen(new TitleScreen());
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
    
    @Override
    protected void renderFailed(GuiGraphics graphics, int mx, int my, String failed) {
        // We don't want to render "⚠ Asset Error" if the player wants just text
    }
    
    @Override
    public boolean keyPressed(int keyCode, int pScanCode, int pModifiers) {
        // Skipping is a little unreliable
        return super.keyPressed(keyCode, pScanCode, pModifiers);
    }
    
    @Override
    protected void advance() {
        super.advance();
        textSkipped = false;
    }
}
