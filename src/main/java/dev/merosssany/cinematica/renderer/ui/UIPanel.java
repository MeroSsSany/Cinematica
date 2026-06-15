package dev.merosssany.cinematica.renderer.ui;

import net.minecraft.client.gui.GuiGraphics;

public interface UIPanel {
    void render(GuiGraphics graphics, int top, int left, int width, int height, int mouseX, int mouseY, float pTick);
    boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY);
    boolean mouseClicked(double mouseX, double mouseY, int button);
}
