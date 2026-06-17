package dev.merosssany.cinematica.renderer.slideshow;

import dev.merosssany.cinematica.core.data.RGBA;
import dev.merosssany.cinematica.core.data.death.DeathScreenContext;
import dev.merosssany.cinematica.core.data.loader.CinematicaProjectLoader;
import dev.merosssany.cinematica.core.data.loader.assets.SlideshowLoader;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSlide;
import dev.merosssany.cinematica.renderer.FadeState;
import dev.merosssany.cinematica.renderer.OverflowData;
import dev.merosssany.cinematica.renderer.Renderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class CineDeathScreen extends SlideshowScreen {
    private static final RGBA VIGNETTE_INITIAL_COLOR = RGBA.fromRGBA(168, 20, 25, 1);
    private final List<Button> exitButtons = new ArrayList<>();
    
    private double lastFrame;
    private double timePassed;
    private double driftX = 0;
    private double driftY = 0;
    private double targetDriftX = 0;
    private double targetDriftY = 0;
    private boolean started;
    private boolean ended;
    private int prevStage = -1;
    private List<String> cache;
    private String cacheSubtext;
    
    protected final DeathScreenContext context;
    private double delta;
    private OverflowData overflow;
    
    public CineDeathScreen(DeathScreenContext context) {
        super(CinematicaProjectLoader.get(context.settings().name(), SlideshowLoader.class));
        this.context = context;
        
        setFadeState(FadeState.FADE_FROM_BLACK);
        lastFrame = glfwGetTime();
    }
    
    @Override
    public void render(GuiGraphics graphics, int mx, int my, float pTick) {
        double current = glfwGetTime();
        delta = current - lastFrame;
        timePassed += delta;
        lastFrame = current;
        
        // Initial Delay logic
        if (timePassed >= 2.5 && !started) {
            resetTime();
            started = true;
        }
        
        if (started && !ended) {
            super.render(graphics, mx, my, pTick);
        } else {
            // FIX: Replaced old hardcoded integer overlay calls with clear explicit depth layer clears
            graphics.fill(0, 0, width, height, 0xFF000000);
        }
        
        if (ended) {
            int x = (int) (width * 0.5f);
            float y = height * 0.25f;
            Renderer.drawScaledString(font, graphics, "You died!", x, (int) y, 2.5f, 0xFFFF0000, true);
            graphics.drawCenteredString(font, settings.name(), x, (int) (height / 2.7f), 0xFFFFFFFF);
            
            for (Renderable renderable : this.renderables) {
                renderable.render(graphics, mx, my, pTick);
            }
        }
        
        if (timePassed < 0.2) Renderer.drawVignette(graphics, width, height, VIGNETTE_INITIAL_COLOR);
    }
    
    @Override
    protected void renderText(GuiGraphics graphics, String subtext, String title, SlideshowSlide currentStage) {
        if (context.settings().useDefaultSlideshow() && !isFailed()) {
            super.renderText(graphics, slideshowContextualize(subtext), title, currentStage);
            return;
        }
        
        if (overflow == null) overflow = Renderer.layoutText(this.font, getStrings(subtext), this.width);
        
        int totalLinesCount = overflow.lines().size();
        int staticTextHeight = totalLinesCount * font.lineHeight + (totalLinesCount - 1);
        int pY = (this.height / 2) - (staticTextHeight / 2);
        
        int pX = this.width / 2;
        int speed = currentStage.typingSpeed();
        
        if (targetDriftX == driftX && targetDriftY == driftY) {
            float angle = ThreadLocalRandom.current().nextFloat(0, (float) (Math.PI * 2));
            float maxRadius = 3.0f;
            float length = ThreadLocalRandom.current().nextFloat(2.0f, maxRadius);
            
            targetDriftX = Math.sin(angle) * length;
            targetDriftY = Math.cos(angle) * length;
        }
        
        float driftSpeed = 0.5f;
        
        if (settings.alternateTextPosition()) {
            driftX = lerp(driftX, targetDriftX, driftSpeed * delta);
            driftY = lerp(driftY, targetDriftY, driftSpeed * delta);
        } else {
            driftX = 0;
            driftY = 0;
        }
        graphics.pose().pushPose();
        // FIX: Cast properties to precise explicit float layers for PoseStack translations
        graphics.pose().translate((float) driftX, (float) driftY, 0.0F);
        
        for (int i = 0; i < overflow.lines().size(); i++) {
            int nativeY = pY + (i * (font.lineHeight + 1));
            FormattedCharSequence line = getFormattedCharSequence(overflow, i, overflow.lines().size(), speed);
            graphics.drawCenteredString(font, line, pX, nativeY, currentStage.textColor() == null ? 0xFFFFFFFF : toHex(currentStage.textColor()));
        }
        
        graphics.pose().popPose();
    }
    
    private static double lerp(double start, double end, double pct) {
        if (pct > 1.0) pct = 1.0;
        return start + (end - start) * pct;
    }
    
    private @NotNull List<String> getStrings(String subtext) {
        if (getStage() != prevStage) {
            prevStage = getStage();
            cache = List.of(contextualize(subtext).split("\n"));
        }
        return cache;
    }
    
    private String slideshowContextualize(String subtext) {
        if (getStage() != prevStage) {
            prevStage = getStage();
            cacheSubtext = contextualize(subtext);
        }
        return cacheSubtext;
    }
    
    private String contextualize(String subtext) {
        LocalPlayer player = getMinecraft().player;
        if (player == null) return subtext;
        
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
        
        return subtext
                .replace("$player", player.getDisplayName().getString())
                .replace("$attacker_health", attackerHealth)
                .replace("$attacker", attackerName)
                .replace("$fps", String.valueOf(getMinecraft().getFps()))
                .replace("$x", String.format("%.1f", player.getX()))
                .replace("$y", String.format("%.1f", player.getY()))
                .replace("$z", String.format("%.1f", player.getZ()))
                .replace("$health", String.format("%.1f", player.getHealth()))
                .replace("$death_message", context.deathMessage())
                .replace("$dimension", dimensionName);
    }
    
    @Override
    protected void reset() {
        super.reset();
        overflow = null;
    }
    
    @Override
    protected void init() {
        super.init();
        this.exitButtons.clear();
        
        Component respawnText = isHardcore() ? Component.translatable("deathScreen.spectate") : Component.translatable("deathScreen.respawn");
        
        Button respawnBtn = Button.builder(respawnText, (btn) -> {
            // FIX: Modern client tracking handles player respawning updates on the connection network channel
            if (this.minecraft.player != null) this.minecraft.player.respawn();
            btn.active = false;
            onClose();
            getMinecraft().setScreen(null);
        }).bounds(this.width / 2 - 100, this.height / 2 + 20, 200, 20).build();
        
        Button titleBtn = Button.builder(Component.translatable("deathScreen.titleScreen"), (btn) -> {
            // FIX: Updated clean, single-action layout closure routing logic for title screen exits
            this.handleExitToTitleScreen();
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
        ended = true;
        setButtonsVisible(true);
        cleanup();
    }
    
    private void handleExitToTitleScreen() {
        if (isHardcore()) {
            this.exitToTitleScreen();
        } else {
            // FIX: Replaced DeathScreen.TitleConfirmScreen with a standard native ConfirmScreen structure
            ConfirmScreen confirm = new ConfirmScreen(
                    (confirmed) -> {
                        if (confirmed) {
                            this.exitToTitleScreen();
                        } else {
                            if (this.minecraft.player != null) this.minecraft.player.respawn();
                            this.minecraft.setScreen(null);
                        }
                    },
                    Component.translatable("deathScreen.quit.confirm"),
                    CommonComponents.EMPTY,
                    Component.translatable("deathScreen.titleScreen"),
                    Component.translatable("deathScreen.respawn")
            );
            this.minecraft.setScreen(confirm);
        }
    }
    
    private boolean isHardcore() {
        return this.minecraft.level != null && this.minecraft.level.getLevelData().isHardcore();
    }
    
    private void exitToTitleScreen() {
        if (this.minecraft.level != null) {
            this.minecraft.level.disconnect();
        }
        
        this.minecraft.disconnect(new GenericMessageScreen(Component.translatable("menu.savingLevel")));
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
    protected void advance() {
        super.advance();
        textSkipped.set(false);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int pScanCode, int pModifiers) {
        if (!started) return false;
        if (keyCode == GLFW_KEY_E) {
            reset();
            end();
            return true;
        }
        return super.keyPressed(keyCode, pScanCode, pModifiers);
    }
}