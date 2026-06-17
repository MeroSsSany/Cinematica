package dev.merosssany.cinematica.renderer.cutscene;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.ClientCameraMemory;
import dev.merosssany.cinematica.core.data.cutscene.CutsceneSettings;
import dev.merosssany.cinematica.core.data.loader.CinematicaProjectLoader;
import dev.merosssany.cinematica.core.data.loader.assets.SlideshowLoader;
import dev.merosssany.cinematica.renderer.slideshow.SlideshowScreen;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class CutsceneScreen extends SlideshowScreen {
    private final List<Integer> appearIn;
    private final CutsceneSettings cutscene;
    
    public CutsceneScreen(CutsceneSettings settings) {
        super(CinematicaProjectLoader.get(settings.inheritSlideshow(), SlideshowLoader.class));
        appearIn = new ArrayList<>();
        this.cutscene = settings;
        
        for (int stage : settings.showAtStage()) appearIn.add(stage);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mx, int my, float pTick) {
        ClientCameraMemory.render = appearIn.contains(getStage());
        super.render(graphics, mx, my, pTick);
    }
    
    @Override
    protected void stopSounds() {
        if (appearIn.contains(getStage()) && !cutscene.frames()[getStage()].containSounds()) super.stopSounds();
    }
}
