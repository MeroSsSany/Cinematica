package dev.merosssany.cinematica.event;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.ClientDeathMemory;
import dev.merosssany.cinematica.core.data.death.DeathScreenContext;
import dev.merosssany.cinematica.core.data.death.DeathScreenSettings;
import dev.merosssany.cinematica.core.data.loader.CinematicaProjectLoader;
import dev.merosssany.cinematica.core.data.loader.assets.DeathScreenLoader;
import dev.merosssany.cinematica.core.data.loader.assets.SlideshowLoader;
import dev.merosssany.cinematica.renderer.slideshow.CineDeathScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = Cinematica.MODID, value = Dist.CLIENT)
public class ClientEvents {
    
    @SubscribeEvent(priority = EventPriority.LOWEST) // Assures Cinematica intercepts after other mods modify layouts
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof DeathScreen && !(event.getScreen() instanceof CineDeathScreen)) {
            
            if (ClientDeathMemory.pendingSceneId != null && !ClientDeathMemory.pendingSceneId.isEmpty()) {
                
                if (CinematicaProjectLoader.get(ClientDeathMemory.pendingSceneId, SlideshowLoader.class) != null) {
                    Minecraft mc = Minecraft.getInstance();
                    ClientLevel level = mc.level;
                    
                    if (level != null) {
                        DeathScreenSettings settings = CinematicaProjectLoader.get(ClientDeathMemory.pendingSceneId, DeathScreenLoader.class);
                        Entity attacker = level.getEntity(ClientDeathMemory.pendingAttackerId);
                        
                        String deathMessage = ClientDeathMemory.message;
                        if (deathMessage == null || deathMessage.isEmpty()) {
                            deathMessage = "Fell out of the sky"; // Thematic backup fallback
                        }
                        
                        event.setNewScreen(new CineDeathScreen(new DeathScreenContext(settings, attacker, deathMessage)));
                    }
                } else {
                    Cinematica.getLogger().error("Requested custom death scene '{}' but it was not registered!", ClientDeathMemory.pendingSceneId);
                }
                
                clearDeathMemory();
            }
        }
    }
    
    private static void clearDeathMemory() {
        ClientDeathMemory.pendingSceneId = "";
        ClientDeathMemory.pendingAttackerId = -1;
        ClientDeathMemory.message = "";
    }
}