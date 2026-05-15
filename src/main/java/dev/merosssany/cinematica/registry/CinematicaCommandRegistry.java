package dev.merosssany.cinematica.registry;

import dev.merosssany.cinematica.ObjectKey;
import dev.merosssany.cinematica.core.data.handler.CinematicaCommandHandler;

import java.util.concurrent.ConcurrentHashMap;

public class CinematicaCommandRegistry extends CinematicaRegistry<String, CinematicaCommandHandler> {
    public CinematicaCommandRegistry(ObjectKey key) {
        super(new ConcurrentHashMap<>(), key);
    }
}
