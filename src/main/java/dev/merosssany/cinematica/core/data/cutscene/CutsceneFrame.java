package dev.merosssany.cinematica.core.data.cutscene;

import org.joml.Vector2f;

public record CutsceneFrame(
        Vector2f rotation,
        Vector2f position,
        double time,
        boolean paused,
        boolean containSounds
) {
}
