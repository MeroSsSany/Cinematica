package dev.merosssany.cinematica.registry;

import dev.merosssany.cinematica.ObjectKey;

public class CinematicaRegistries {
    private static final ObjectKey key = new ObjectKey();
    
    public static final CinematicaCommandRegistry COMMAND_REGISTRY = new CinematicaCommandRegistry(key);
    public static final DeathScreenRegistry DEATH_SCREEN_REGISTRY = new DeathScreenRegistry(key);
}
