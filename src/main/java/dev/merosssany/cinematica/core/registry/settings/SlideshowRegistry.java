package dev.merosssany.cinematica.core.registry.settings;

import dev.merosssany.cinematica.core.security.ObjectKey;
import dev.merosssany.cinematica.core.data.slideshow.SlideshowSettings;
import dev.merosssany.cinematica.core.registry.CinematicaRegistry;

import java.util.concurrent.ConcurrentHashMap;

public class SlideshowRegistry extends CinematicaRegistry<String, SlideshowSettings> {
    public SlideshowRegistry(ObjectKey key) {
        super(new ConcurrentHashMap<>(), key);
    }
    
    public void register(SlideshowSettings settings) {
        register(settings.name(), settings);
    }
}
