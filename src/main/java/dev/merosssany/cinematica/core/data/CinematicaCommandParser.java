package dev.merosssany.cinematica.core.data;

import dev.merosssany.cinematica.core.data.handler.CinematicaCommandHandler;
import dev.merosssany.cinematica.registry.CinematicaCommandRegistry;
import dev.merosssany.cinematica.registry.CinematicaRegistries;

public class CinematicaCommandParser {
    private static final CinematicaCommandParser instance = new CinematicaCommandParser(CinematicaRegistries.COMMAND_REGISTRY);
    protected final CinematicaCommandRegistry registry;
    
    public CinematicaCommandParser(CinematicaCommandRegistry registry) {
        this.registry = registry;
    }
    
    public static CinematicaCommandParser get() {
        return instance;
    }
    
    public boolean run(String command, CinematicaCommandContext context) {
        CinematicaCommandHandler handler = registry.get(command);
        return handler.run(context);
    }
}
