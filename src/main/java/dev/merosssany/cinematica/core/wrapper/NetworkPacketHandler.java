package dev.merosssany.cinematica.core.wrapper;

import dev.merosssany.cinematica.core.data.CinematicaSettings;

import java.util.UUID;

public interface NetworkPacketHandler {
    void sendSettings(CinematicaSettings settings, UUID playerUuid);
}
