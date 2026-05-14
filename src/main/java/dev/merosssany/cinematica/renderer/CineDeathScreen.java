package dev.merosssany.cinematica.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.rendering.TextureInfo;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSlide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class CineDeathScreen extends SlideshowScreen {
    private double lastFrame;
    private double timePassed;
    private final List<Button> exitButtons = new ArrayList<>();
    
    public CineDeathScreen(SlideshowSettings settings) {
        super(settings);
        
        setFadeState(FadeState.FADE_FROM_BLACK);
        lastFrame = glfwGetTime();
    }
    
    @Override
    public void render(GuiGraphics graphics, int mx, int my, float pTick) {
        double current = glfwGetTime();
        timePassed += current - lastFrame;
        lastFrame = current;
        
        if (timePassed == 2.5) resetTime();
        if (timePassed > 2.5) super.render(graphics, mx, my, pTick);
        else graphics.fill(0,0,width,height,0xFF000000);
    }
    
    @Override
    protected TextureInfo loadTexture(FileInputStream stream, String locationName, int stage) throws IOException {
        NativeImage image = NativeImage.read(stream);
        DynamicTexture tex = new DynamicTexture(image);
        
        // Blur image
        int radius = 3; // Increase for more blur
        blurImage(radius, image);
        
        float imgW = image.getWidth();
        float imgH = image.getHeight();
        
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Cinematica.MODID, locationName + "_" + stage);
        image.close();
        
        Minecraft.getInstance().getTextureManager().register(location, tex);
        return new TextureInfo(imgW, imgH, tex, location);
    }
    
    protected static void blurImage(int radius, NativeImage image) {
        for (int i = 0; i < radius; i++) {
            NativeImage temp = new NativeImage(image.format(), image.getWidth(), image.getHeight(), false);
            for (int y = 1; y < image.getHeight() - 1; y++) {
                for (int x = 1; x < image.getWidth() - 1; x++) {
                    int c1 = image.getPixelRGBA(x, y);
                    int c2 = image.getPixelRGBA(x - 1, y);
                    int c3 = image.getPixelRGBA(x + 1, y);
                    int c4 = image.getPixelRGBA(x, y - 1);
                    int c5 = image.getPixelRGBA(x, y + 1);
                    
                    // Average the ABGR channels
                    int a = ((c1 >> 24 & 0xFF) + (c2 >> 24 & 0xFF) + (c3 >> 24 & 0xFF) + (c4 >> 24 & 0xFF) + (c5 >> 24 & 0xFF)) / 5;
                    int b = ((c1 >> 16 & 0xFF) + (c2 >> 16 & 0xFF) + (c3 >> 16 & 0xFF) + (c4 >> 16 & 0xFF) + (c5 >> 16 & 0xFF)) / 5;
                    int g = ((c1 >> 8 & 0xFF) + (c2 >> 8 & 0xFF) + (c3 >> 8 & 0xFF) + (c4 >> 8 & 0xFF) + (c5 >> 8 & 0xFF)) / 5;
                    int r = ((c1 & 0xFF) + (c2 & 0xFF) + (c3 & 0xFF) + (c4 & 0xFF) + (c5 & 0xFF)) / 5;
                    
                    temp.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
                }
            }
            
            image.copyFrom(temp);
            temp.close();
        }
    }
    
    @Override
    protected void renderText(GuiGraphics graphics, String subtext, String title, SlideshowSlide currentStage) {
        OverflowData overflow = Renderer.layoutText(this.font, List.of(subtext.split("\n")), this.width);
        
        // 2. Vertical Centering
        int pY = (this.height / 2) - (overflow.height() / 2);
        int pX = this.width / 2;
        int speed = currentStage.typingSpeed();
        
        // 3. Draw Lines
        for (int i = 0; i < overflow.lines().size(); i++) {
            FormattedCharSequence line = getFormattedCharSequence(overflow, i, overflow.lines().size(), speed);
            
            graphics.drawCenteredString(font, line, pX, pY + (i * (font.lineHeight + 1)), toHex(currentStage.textColor()));
        }
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
        if (total -1 == i) setTyping(finalCharsToShow == maxChars);
        
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
        super.init(); // Starts your music and init logic
        this.exitButtons.clear();
        
        // Create the buttons but keep them hidden/inactive initially
        Component respawnText = isHardcore() ? Component.translatable("deathScreen.spectate") : Component.translatable("deathScreen.respawn");
        
        Button respawnBtn = Button.builder(respawnText, (btn) -> {
            this.minecraft.player.respawn();
            btn.active = false;
        }).bounds(this.width / 2 - 100, this.height / 2 + 20, 200, 20).build();
        
        Button titleBtn = Button.builder(Component.translatable("deathScreen.titleScreen"), (btn) -> {
            this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, this::handleExitToTitleScreen, true);
        }).bounds(this.width / 2 - 100, this.height / 2 + 45, 200, 20).build();
        
        this.exitButtons.add(this.addRenderableWidget(respawnBtn));
        this.exitButtons.add(this.addRenderableWidget(titleBtn));
        
        // Start them as invisible so they don't ruin the slideshow
        setButtonsVisible(false);
    }
    
    private void setButtonsVisible(boolean visible) {
        for (Button btn : exitButtons) {
            btn.visible = visible;
            btn.active = visible;
        }
    }
    
    @Override
    protected void end() {
        // This is called by SlideshowScreen when the last slide is done
        setButtonsVisible(true);
    }
    
    // --- Vanilla Logic Ported ---
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
}
