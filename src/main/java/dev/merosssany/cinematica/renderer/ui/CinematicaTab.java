package dev.merosssany.cinematica.renderer.ui;

import dev.merosssany.cinematica.core.data.RGBA;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class CinematicaTab extends CinematicaButton {
    private final int index;
    private boolean active = false;

    public CinematicaTab(int index, int pX, int pY, int pWidth, int pHeight, RGBA color, Component pMessage, Font font) {
        super(pX, pY, pWidth, pHeight, color, pMessage, font);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        int currentX = getX();
        int currentY = getY();
        
        // Use a brighter/darker outline depending on hover or active states
        boolean highlighting = isHovered() || active;
        int borderHex = highlighting ? 0xFFFFFFFF : lineColor.toHexadecimalARGB();
        
        // Background fill color
        int fillHex = color.toHexadecimalARGB();
        
        if (active) {
            // Draw a tab that opens downward (top, left, and right borders only)
            graphics.fill(currentX, currentY, currentX + width, currentY + height, fillHex);
            graphics.vLine(currentX, currentY, currentY + height, borderHex); // Left
            graphics.vLine(currentX + width - 1, currentY, currentY + height, borderHex); // Right
            graphics.hLine(currentX, currentX + width, currentY, borderHex); // Top
        } else {
            // Standard inactive closed button frame
            graphics.renderOutline(currentX, currentY, width, height, borderHex);
            graphics.fill(currentX + 1, currentY + 1, currentX + width - 1, currentY + height - 1, fillHex);
        }

        // Text coloring using your lightness rules
        int textHex = active ? 0xFFFFFFFF : 0xAAAAAA;
        int centerX = currentX + (width / 2);
        int centerY = currentY + (height / 2) - (font.lineHeight / 2);
        
        graphics.drawCenteredString(font, getMessage(), centerX, centerY, textHex);
    }

    @Override
    public boolean clicked() {
        return true; // Used by screen click handler
    }
}