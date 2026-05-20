package dev.merosssany.cinematica.renderer.ui;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.RGBA;
import dev.merosssany.cinematica.renderer.Renderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.resources.ResourceLocation;

public class CinematicaHeader implements Renderable, NarratableEntry, GuiEventListener {
    public static final ResourceLocation CINEMATICA_DARK = ResourceLocation.fromNamespaceAndPath(Cinematica.modId, "textures/icon_dark.png");
    public static final ResourceLocation CINEMATICA = ResourceLocation.fromNamespaceAndPath(Cinematica.modId, "textures/icon.png");
    
    protected final Font font;
    protected final RGBA color;
    protected final RGBA lineColor;
    protected String header;
    protected boolean dark;
    protected int x, y, width, height;
    
    public CinematicaHeader(boolean dark, int x, int y, int width, int height, Font font, String header, RGBA color) {
        this.dark = dark;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.font = font;
        this.header = header;
        this.color = color;
        lineColor = color.getLightnessInvertedColor();
    }
    
    public boolean isDark() {
        return dark;
    }
    
    public void setDark(boolean dark) {
        this.dark = dark;
    }
    
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public String getHeader() {
        return header;
    }
    
    public void setHeader(String header) {
        this.header = header;
    }
    
    public RGBA getColor() {
        return color;
    }
    
    public RGBA getLineColor() {
        return lineColor;
    }
    
    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        int currentHeight = getHeight();
        
        graphics.blit(
                isDark() ? CINEMATICA_DARK : CINEMATICA,
                this.x, this.y,
                0.0F, 0.0F,
                currentHeight, currentHeight,
                currentHeight, currentHeight
        );
        
        float scale = (float) currentHeight / (float) font.lineHeight;
        
        Renderer.drawScaledString(
                font, graphics,
                getHeader(),
                this.x + currentHeight + 4,
                this.y,
                scale,
                color.toHexadecimalARGB(),
                false
        );
        
        int lineY = this.y + currentHeight + 3;
        graphics.hLine(
                this.x,              // Start line at component X offset
                this.x + this.width, // End line at component total span width
                lineY,
                lineColor.toHexadecimalARGB()
        );
    }
    
    @Override
    public void setFocused(boolean pFocused) {
    
    }
    
    @Override
    public boolean isFocused() {
        return false;
    }
    
    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }
    
    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
    
    }
}
