package dev.merosssany.cinematica.renderer.ui;

import dev.merosssany.cinematica.core.data.RGBA;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class CinematicaButton extends AbstractWidget {
    protected final RGBA color;
    protected final RGBA lineColor;
    protected final Font font;
    
    public CinematicaButton(int pX, int pY, int pWidth, int pHeight, RGBA color, Component pMessage, Font font) {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.color = color;
        this.lineColor = color;
        this.font = font;
    }
    
    public RGBA getColor() {
        return color;
    }
    
    public RGBA getLineColor() {
        return lineColor;
    }
    
    @Override
    protected void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        int currentX = getX();
        int currentY = getY();
        
        boolean hovering = isHovered();
        int lc = hovering ? 0xFFFFFFFF : lineColor.toHexadecimalARGB();
        
        graphics.renderOutline(currentX, currentY, this.width, this.height, lc);
        
        int absoluteX2 = currentX + this.width - 1;
        int absoluteY2 = currentY + this.height - 1;
        graphics.fill(currentX + 1, currentY + 1, absoluteX2, absoluteY2, color.toHexadecimalARGB());
        
        int centerX = currentX + (this.width / 2);
        int centerY = currentY + (this.height / 2) - (font.lineHeight / 2);
        
        graphics.drawCenteredString(font, getMessage(), centerX, centerY, 0xFFFFFFFF);
    }
    
    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    
    }
    
    public abstract boolean clicked();
}
