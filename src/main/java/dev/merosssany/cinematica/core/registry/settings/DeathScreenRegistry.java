package dev.merosssany.cinematica.core.registry.settings;

import dev.merosssany.cinematica.core.security.ObjectKey;
import dev.merosssany.cinematica.core.data.death.DeathScreenSettings;
import dev.merosssany.cinematica.core.registry.CinematicaRegistry;

import java.util.concurrent.ConcurrentHashMap;

public class DeathScreenRegistry extends CinematicaRegistry<String, DeathScreenSettings> {
    public DeathScreenRegistry(ObjectKey key) {
        super(new ConcurrentHashMap<>(), key);
    }
    
    public void register(DeathScreenSettings settings) {
        register(settings.name(), settings);
    }
}
