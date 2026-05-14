package dev.merosssany.cinematica.registry;

import dev.merosssany.cinematica.ObjectKey;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.ConcurrentHashMap;

public class DeathScreenRegistry extends CinematicaRegistry<ResourceLocation, SlideshowSettings> {
    public DeathScreenRegistry(ObjectKey key) {
        super(new ConcurrentHashMap<>(), key);
    }
}
