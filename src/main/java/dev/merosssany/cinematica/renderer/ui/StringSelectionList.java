package dev.merosssany.cinematica.renderer.ui;

import dev.merosssany.cinematica.core.data.RGBA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

public class StringSelectionList extends ObjectSelectionList<StringSelectionList.StringEntry> {
    public StringSelectionList(Minecraft p_94442_, int p_94443_, int p_94444_, int p_94445_, int p_94446_) {
        super(p_94442_, p_94443_, p_94444_, p_94445_, p_94446_);
    }
    
    public void addString(String message, RGBA baseColor, int id) {
        this.addEntry(new StringEntry(
                Component.literal(message),
                baseColor,
                this.minecraft.font,
                id
        ));
    }
    
    public int id() {
        StringEntry entry = getSelected();
        
        if (entry == null) return -1;
        else return entry.id;
    }
    
    protected static class StringEntry extends Entry<StringEntry> {
        private final Component text;
        private final RGBA color;
        private final RGBA invertedColor;
        private final RGBA contrastColor;
        private final Font font;
        public final int id;
        
        public StringEntry(Component text, RGBA color, Font font, int id) {
            this.text = text;
            this.color = color;
            this.font = font;
            this.id = id;
            invertedColor = color.getLightnessInvertedColor();
            contrastColor = color.getContrastColor();
        }
        
        @Override
        public Component getNarration() {
            return text;
        }
        
        @Override
        public void render(GuiGraphics graphics, int pIndex, int top, int left, int width, int height, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            int absoluteX2 = left + width - 1;
            int absoluteY2 = top + height - 1;
            
            graphics.fill(left + 1, top + 1, absoluteX2, absoluteY2, color.toHexadecimalARGB());
            graphics.renderOutline(left, top, width, height, invertedColor.toHexadecimalARGB());
            
            int textX = left + 6;
            int textY = top + (height / 2) - (font.lineHeight / 2);
            graphics.drawString(font, this.text, textX, textY, contrastColor.toHexadecimalARGB(), false);
        }
    }
}
