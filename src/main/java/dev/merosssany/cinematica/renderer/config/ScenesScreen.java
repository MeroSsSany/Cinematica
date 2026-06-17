package dev.merosssany.cinematica.renderer.config;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.RGBA;
import dev.merosssany.cinematica.core.data.loader.CinematicaProjectLoader;
import dev.merosssany.cinematica.core.data.loader.assets.SlideshowLoader;
import dev.merosssany.cinematica.renderer.ui.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ScenesScreen extends Screen {
    private static final Logger log = LoggerFactory.getLogger(ScenesScreen.class);
    private boolean darkMode = true;
    private TabManager tabs;
    
    public ScenesScreen() {
        super(Component.literal("Cinematica Config Screen"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        int panelHeight = height - 62;
        tabs = new TabManager(0,32,90, 20, width, panelHeight,font, RGBA.fromHex(0x00263a));
        tabs.put(Component.literal("Slideshow"), new Slideshow(minecraft, width, panelHeight, 32, height - 30));
        
        addRenderableWidget(new CinematicaHeader(darkMode, 0, 0, width, 32, font, "Cinematica Configuration", new RGBA(1,1,1,1)));
        addRenderableWidget(tabs);
    }
    
    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        graphics.fill(0, 0, width, height, 0xff010c1c);
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tabs.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private static class Slideshow implements UIPanel {
        private final Minecraft minecraft;
        private final StringSelectionList list;
        private int id;
        private int id() {return id++;}
        
        public Slideshow(Minecraft minecraft, int width, int height, int top, int bottom) {
            this.minecraft = minecraft;
            list = new StringSelectionList(minecraft, width, height, top, bottom);
            
            Set<String> slideshows = CinematicaProjectLoader.getLoader(SlideshowLoader.class).getRegistered();
            List<String> sorted = new ArrayList<>(List.copyOf(slideshows));
            
            Collections.sort(sorted);
            
            for (String slideshow : sorted) {
                list.addString(slideshow, RGBA.fromRGBA(8, 18, 33, 255), id());
            }
        }
        
        @Override
        public void render(GuiGraphics graphics, int top, int left, int width, int height, int mouseX, int mouseY, float pTick) {
            list.render(graphics, mouseX, mouseY, pTick);
        }
        
        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            return list.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return list.mouseClicked(mouseX, mouseY, button);
        }
    }
}
