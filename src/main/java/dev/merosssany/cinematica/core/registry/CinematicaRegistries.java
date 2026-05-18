package dev.merosssany.cinematica.core.registry;

import dev.merosssany.cinematica.core.security.ObjectKey;

public class CinematicaRegistries {
    private static final ObjectKey key = new ObjectKey();
    
    public static final CinematicaCommandRegistry COMMAND_REGISTRY = new CinematicaCommandRegistry(key);
}
