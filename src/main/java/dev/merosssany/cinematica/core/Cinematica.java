package dev.merosssany.cinematica.core;

import dev.merosssany.cinematica.core.data.CinematicaSettings;
import dev.merosssany.cinematica.core.wrapper.NetworkPacketHandler;

public class Cinematica {
    public static final String MODID = "cinematica";
    
    private final CinematicaSettings settings;
    private final NetworkPacketHandler networkHandler;
    
    public Cinematica(CinematicaSettings settings, NetworkPacketHandler networkHandler) {
        this.settings = settings;
        this.networkHandler = networkHandler;
    }
}
