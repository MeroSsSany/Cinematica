package dev.merosssany.cinematica.event;

import dev.merosssany.cinematica.core.Cinematica;
import dev.merosssany.cinematica.core.data.loader.CinematicaProjectLoader;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(modid = Cinematica.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ModBusEvents {
    
    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
                return null;
            }
            
            @Override
            protected void apply(Void object, ResourceManager manager, ProfilerFiller profiler) {
                CinematicaProjectLoader.loadProjects(manager);
            }
        });
    }
}