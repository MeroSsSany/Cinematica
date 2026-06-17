package dev.merosssany.cinematica.core.registry.settings;

import dev.merosssany.cinematica.core.data.cutscene.CutsceneSettings;
import dev.merosssany.cinematica.core.registry.CinematicaRegistry;
import dev.merosssany.cinematica.core.security.ObjectKey;

import java.util.concurrent.ConcurrentHashMap;

public class CutsceneRegistry extends CinematicaRegistry<String, CutsceneSettings> {
    public CutsceneRegistry(ObjectKey key) {
        super(new ConcurrentHashMap<>(), key);
    }
    
    public void register(CutsceneSettings settings) {
        register(settings.name(), settings);
    }
}
