package dev.merosssany.cinematica.event;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.ClientDeathMemory;
import dev.merosssany.cinematica.core.data.death.DeathScreenContext;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import dev.merosssany.cinematica.mixin.CombatTrackerAccessor;
import dev.merosssany.cinematica.registry.CinematicaRegistries;
import dev.merosssany.cinematica.registry.capablities.CinematicCapProvider;
import dev.merosssany.cinematica.registry.capablities.ICinematicCap;
import dev.merosssany.cinematica.renderer.CineDeathScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Cinematica.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof DeathScreen && !(event.getScreen() instanceof CineDeathScreen)) {
            // Did the server tell us a specific cinematic to play?
            if (!ClientDeathMemory.pendingSceneId.isEmpty() && Cinematica.SlideshowExists(ClientDeathMemory.pendingSceneId)) {
                
                SlideshowSettings settings = Cinematica.getSlideshow(ClientDeathMemory.pendingSceneId);
                Entity attacker = Minecraft.getInstance().level.getEntity(ClientDeathMemory.pendingAttackerId);
                
                // We pass the data to your custom screen
                event.setNewScreen(new CineDeathScreen(new DeathScreenContext(settings, attacker, ClientDeathMemory.message)));
                
                // Clear the memory so it doesn't trigger again on the next death
                ClientDeathMemory.pendingSceneId = "";
                ClientDeathMemory.pendingAttackerId = -1;
                ClientDeathMemory.message = "";
            }
        }
    }
}
