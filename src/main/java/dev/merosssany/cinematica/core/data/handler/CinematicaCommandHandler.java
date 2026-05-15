package dev.merosssany.cinematica.core.data.handler;

import dev.merosssany.cinematica.core.data.CinematicaCommandContext;

public interface CinematicaCommandHandler {
    boolean run(CinematicaCommandContext context);
}
