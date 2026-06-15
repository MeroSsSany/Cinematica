package dev.merosssany.cinematica.renderer.ui;

import dev.merosssany.cinematica.core.data.RGBA;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dev.merosssany.cinematica.renderer.Renderer.inBounds;

public class TabManager extends AbstractWidget {
    protected List<CinematicaTab> tabList = new ArrayList<>();
    protected List<UIPanel> panels = new ArrayList<>();
    protected int index = 0;
    protected int active = 0;
    protected int x, y, tabWidth, tabHeight;
    protected Font font;
    protected RGBA backgroundColor;
    protected int panelWidth;
    protected int panelHeight;
    
    public TabManager(int x, int y, int tabWidth, int tabHeight, int panelWidth, int panelHeight, Font font, RGBA backgroundColor) {
        super(x, y, panelWidth, panelHeight + tabHeight, Component.literal(""));
        this.x = x;
        this.y = y;
        this.tabWidth = tabWidth;
        this.tabHeight = tabHeight;
        this.font = font;
        this.backgroundColor = backgroundColor;
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
    }
    
    public void put(Component tabName, UIPanel panel) {
        TabManager instance = this;
        
        final int currentTabIndex = this.index;
        CinematicaTab tab = getCinematicaTab(tabName, currentTabIndex, instance);
        
        panels.add(panel);
        tabList.add(tab);
        
        this.index++;
    }
    
    private @NotNull CinematicaTab getCinematicaTab(Component tabName, int currentTabIndex, TabManager instance) {
        CinematicaTab tab = new CinematicaTab(currentTabIndex, x + (tabWidth * currentTabIndex), y, tabWidth, tabHeight, backgroundColor, tabName, font) {
            @Override
            public void clicked() {
                instance.setActiveTab(currentTabIndex);
            }
        };
        
        if (currentTabIndex == 0) {
            tab.setActive(true);
        }
        return tab;
    }
    
    public void setActiveTab(int indexToActivate) {
        if (indexToActivate < 0 || indexToActivate >= tabList.size()) return;
        
        this.active = indexToActivate;
        for (int i = 0; i < tabList.size(); i++) {
            tabList.get(i).setActive(i == indexToActivate);
        }
    }
    
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        panels.get(active).render(guiGraphics, y + tabHeight, x, panelWidth, panelHeight, mouseX, mouseY, partialTick);
        
        if (tabList.isEmpty() || panels.isEmpty()) return;
        
        for (CinematicaTab tab : tabList) {
            tab.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inBounds(x, y + tabHeight, panelWidth, panelHeight, (int) mouseX, (int) mouseY)) {
            if (panels.get(active).mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        
        for (CinematicaTab tab : tabList) {
            if (tab.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return mouseScrolled(mouseX, mouseY, 0, delta);
    }
    
    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
    
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (panels.isEmpty() || active >= panels.size()) return false;
        
        if (inBounds(x, y + tabHeight, panelWidth, panelHeight, (int) mouseX, (int) mouseY)) {
            return panels.get(active).mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return false;
    }
}