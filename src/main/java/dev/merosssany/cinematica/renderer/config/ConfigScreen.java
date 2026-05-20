package dev.merosssany.cinematica.renderer.config;

import dev.merosssany.cinematica.core.data.RGBA;
import dev.merosssany.cinematica.renderer.ui.CinematicaHeader;
import dev.merosssany.cinematica.renderer.ui.InputScrollList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
    private InputScrollList list;
    private boolean darkMode = true;
    
    public ConfigScreen() {
        super(Component.literal("Cinematica Config Screen"));
    }
    
    @Override
    protected void init() {
        super.init();
        list = new InputScrollList(minecraft, width, height, 32, height - 40, 30);
        
        addRenderableWidget(list);
        addRenderableWidget(new CinematicaHeader(darkMode, 0, 0, width, 32, font, "Cinematica Configuration", new RGBA(1,1,1,1)));
    }
}
