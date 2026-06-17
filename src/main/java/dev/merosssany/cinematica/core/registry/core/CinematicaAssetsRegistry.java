package dev.merosssany.cinematica.core.registry.core;

import dev.merosssany.cinematica.core.data.loader.CinematicaAssetLoader;
import dev.merosssany.cinematica.core.registry.CinematicaRegistry;
import dev.merosssany.cinematica.core.security.ObjectKey;

import java.util.concurrent.ConcurrentHashMap;

public class CinematicaAssetsRegistry extends CinematicaRegistry<String, CinematicaAssetLoader<?>> {
    public CinematicaAssetsRegistry(ObjectKey key) {
        super(new ConcurrentHashMap<>(), key);
    }
    
    @Override
    public void register(String s, CinematicaAssetLoader<?> cinematicaAssetLoader) {
        if (entries.containsValue(cinematicaAssetLoader)) return;
        super.register(s, cinematicaAssetLoader);
    }
}
